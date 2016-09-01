package org.tuxdevelop.spring.batch.lightmin.client.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tuxdevelop.spring.batch.lightmin.client.ITConfigurationApplication;
import org.tuxdevelop.spring.batch.lightmin.client.registration.LightminClientRegistrator;
import org.tuxdevelop.spring.batch.lightmin.client.registration.RegistrationLightminClientApplicationBean;
import org.tuxdevelop.spring.batch.lightmin.client.registration.listener.OnApplicationReadyEventListener;
import org.tuxdevelop.spring.batch.lightmin.client.registration.listener.OnContextClosedEventListener;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest({"server.port=0", "management.port=0"})
@SpringApplicationConfiguration(classes = {ITConfigurationApplication.class, LightminClientConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LightminClientConfigurationIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testClientConfiguration() {
        final LightminClientRegistrator lightminClientRegistrator = applicationContext.getBean(LightminClientRegistrator.class);
        assertThat(lightminClientRegistrator).isNotNull();
        final RegistrationLightminClientApplicationBean registrationLightminClientApplicationBean =
                applicationContext.getBean(RegistrationLightminClientApplicationBean.class);
        assertThat(registrationLightminClientApplicationBean).isNotNull();
        final OnApplicationReadyEventListener onApplicationReadyEventListener = applicationContext.getBean
                (OnApplicationReadyEventListener.class);
        assertThat(onApplicationReadyEventListener).isNotNull();
        final OnContextClosedEventListener onContextClosedEventListener = applicationContext.getBean
                (OnContextClosedEventListener.class);
        assertThat(onContextClosedEventListener).isNotNull();

    }

}
