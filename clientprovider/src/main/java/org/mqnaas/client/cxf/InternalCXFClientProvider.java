package org.mqnaas.client.cxf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.common.util.ProxyClassLoader;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.mqnaas.clientprovider.api.apiclient.IInternalAPIClientProvider;
import org.mqnaas.clientprovider.exceptions.ClientConfigurationException;
import org.mqnaas.core.api.Credentials;
import org.mqnaas.core.api.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an example implementation of how to implement a specific api provider
 * 
 * @param <CC>
 *            Specific {@link CXFConfiguration} for the client being created. The goal of this parameterization is to allow specific client providers
 *            extending this class to initialize client configuration by extending the {@link CXFConfiguration} class.
 * 
 * @author Adrian Rosello Rey (i2CAT) - authentication and async http conduit.
 * 
 */
public class InternalCXFClientProvider<CC extends CXFConfiguration> implements IInternalAPIClientProvider<CC> {

	@Override
	public String[] getProtocols() {
		// HTTP and secured HTTP endpoints
		return new String[] { "http", "https" };
	}

	private static final Logger	log	= LoggerFactory.getLogger(InternalCXFClientProvider.class);

	@Override
	public <API> API getClient(Class<API> apiClass, Endpoint ep, Credentials c) throws ClientConfigurationException {
		return getClient(apiClass, ep, c, null, null);
	}

	@Override
	public <API> API getClient(Class<API> apiClass, Endpoint ep, Credentials c, CC configuration) throws ClientConfigurationException {
		return getClient(apiClass, ep, c, configuration, null);
	}

	@Override
	public <API> API getClient(Class<API> apiClass, Endpoint ep, Credentials c, CC configuration,
			Object applicationSpecificConfiguration) throws ClientConfigurationException {

		if (configuration != null) {
			if (configuration.getUseDummyClient())
				return createDummyClient(apiClass);
		}

		if (ep == null || ep.getUri() == null) {
			// FIXME fail gracefully
			log.warn("Attempt to create JAX-RS client without target address. Using dummyClient instead");
			return createDummyClient(apiClass);
		}

		// String switchId = (String) sessionContext.getSessionParameters().get(FloodlightProtocolSession.SWITCHID_CONTEXT_PARAM_NAME);
		// TODO use switch id to instantiate the client

		// create CXF client
		ProxyClassLoader classLoader = new ProxyClassLoader();
		classLoader.addLoader(apiClass.getClassLoader());
		classLoader.addLoader(JAXRSClientFactoryBean.class.getClassLoader());

		JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
		bean.setAddress(ep.getUri().toString());

		if (configuration != null && !configuration.getProviders().isEmpty())
			bean.setProviders(configuration.getProviders());

		bean.setResourceClass(apiClass);
		bean.setClassLoader(classLoader);

		API api = bean.create(apiClass);

		// disable CN check
		if (configuration != null && !configuration.isCNCheckEnabled()) {
			TLSClientParameters clientParams = new TLSClientParameters();
			clientParams.setDisableCNCheck(true);
			WebClient.getConfig(api).getHttpConduit().setTlsClientParameters(clientParams);

		}

		// authentication system
		if (configuration != null && configuration.getAuthentication() != null) {

			FileInputStream keystoreFis = null;
			FileInputStream truststoreFis = null;

			try {
				if (configuration.getAuthentication() instanceof CertificatesAuthentication) {

					CertificatesAuthentication auth = (CertificatesAuthentication) configuration.getAuthentication();

					// get TLSClientParameters from cxf bean. If not exists, create it
					TLSClientParameters clientParams = WebClient.getConfig(api).getHttpConduit().getTlsClientParameters();
					if (clientParams == null)
						clientParams = new TLSClientParameters();

					// TODO passwords and keystore should be set in credentials
					// load keystore
					KeyStore keyStore = KeyStore.getInstance("JKS");
					File keyStoreFile = new File(auth.getKeyStoreUri().toString());
					keystoreFis = new FileInputStream(keyStoreFile);
					keyStore.load(keystoreFis, auth.getKeyStorePassword());
					KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					keyFactory.init(keyStore, auth.getKeyStorePassword());
					KeyManager[] km = keyFactory.getKeyManagers();
					clientParams.setKeyManagers(km);

					// load truststore
					File truststore = new File(auth.getTrustStoreUri().toString());
					truststoreFis = new FileInputStream(truststore);
					keyStore.load(truststoreFis, auth.getTrustStorePassword());
					TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustFactory.init(keyStore);
					TrustManager[] tm = trustFactory.getTrustManagers();
					clientParams.setTrustManagers(tm);

					// set tlsclientparameters with certificates information into cxf bean
					WebClient.getConfig(api).getHttpConduit().setTlsClientParameters(clientParams);

				}
			} catch (Exception e) {
				log.error("Error creating CXF client.", e);
				throw new ClientConfigurationException(e);
			} finally {
				try {
					if (keystoreFis != null)
						keystoreFis.close();
					if (truststoreFis != null)
						truststoreFis.close();
				} catch (IOException e) {
					log.warn("Failed to close FileInputStream.", e);
				}

			}
		}

		if (configuration != null && configuration.isUsingAsyncHttpConduit())
			// By enabling async http conduit, as side-effect, support for @Delete methods with body is available.
			// https://issues.apache.org/jira/browse/CXF-5337
			WebClient.getConfig(api).getRequestContext().put("use.async.http.conduit", true);

		return api;
	}

	private <API> API createDummyClient(Class<API> apiClass) {

		ProxyClassLoader classLoader = new ProxyClassLoader();
		classLoader.addLoader(apiClass.getClassLoader());

		// It is safe to cast returned proxy to one of the interfaces given to newProxyInstance method, according to its contract:
		// Proxy.newProxyInstance javadoc:
		// @return a proxy instance with the specified invocation handler of a
		// proxy class that is defined by the specified class loader
		// and that implements the specified interfaces
		@SuppressWarnings("unchecked")
		API dummyClient = (API) Proxy.newProxyInstance(classLoader, new Class[] { apiClass }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				log.debug("Invoking cxf api method " + method + " with args " + args);
				return null;
			}
		});

		return dummyClient;
	}

}
