
package com.maveric.hr360.config;

import com.maveric.hr360.entity.IdentifiedEntity;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Objects;

@Component
public class CustomIdGenerator extends AbstractMongoEventListener<IdentifiedEntity> {
	private final SecureRandom random = new SecureRandom();
	@Override
	public void onBeforeConvert(BeforeConvertEvent<IdentifiedEntity> event) {
		super.onBeforeConvert(event);
		IdentifiedEntity domain = event.getSource();
		if (Objects.isNull(domain.getId())) {
			domain.setId(Math.abs(random.nextLong(1000000000)));
		}
	}

}
