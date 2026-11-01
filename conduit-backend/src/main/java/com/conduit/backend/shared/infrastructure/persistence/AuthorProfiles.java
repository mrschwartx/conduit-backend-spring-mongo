package com.conduit.backend.shared.infrastructure.persistence;

import com.conduit.backend.shared.domain.Profile;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthorProfiles {

	private final EntityManager entityManager;

	public AuthorProfiles(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional(readOnly = true)
	public Map<UUID, Profile> load(Collection<UUID> authorIds, UUID viewerId) {
		if (authorIds.isEmpty()) {
			return Map.of();
		}
		Set<UUID> followed = viewerId == null ? Set.of() : followed(authorIds, viewerId);
		List<?> rows = entityManager
				.createNativeQuery("select id, username, bio, image from users where id in (:ids)")
				.setParameter("ids", authorIds)
				.getResultList();
		Map<UUID, Profile> profiles = new HashMap<>();
		for (Object row : rows) {
			Object[] columns = (Object[]) row;
			UUID id = (UUID) columns[0];
			profiles.put(id, new Profile((String) columns[1], (String) columns[2], (String) columns[3],
					followed.contains(id)));
		}
		return profiles;
	}

	private Set<UUID> followed(Collection<UUID> authorIds, UUID viewerId) {
		List<?> rows = entityManager
				.createNativeQuery("select followee_id from follows where follower_id = :viewer and followee_id in (:ids)")
				.setParameter("viewer", viewerId)
				.setParameter("ids", authorIds)
				.getResultList();
		Set<UUID> followed = new HashSet<>();
		for (Object row : rows) {
			followed.add((UUID) row);
		}
		return followed;
	}
}
