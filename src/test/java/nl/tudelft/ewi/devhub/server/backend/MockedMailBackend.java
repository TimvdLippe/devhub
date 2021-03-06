package nl.tudelft.ewi.devhub.server.backend;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.mail.MailBackend;

import com.google.inject.Singleton;

@Slf4j
@Singleton
public class MockedMailBackend implements MailBackend {

	@Override
	public int getQueueSize() {
		return 0;
	}

	@Override
	public void sendMail(Mail mail) {
		log.info("Sending mail: {}", mail);
	}

}
