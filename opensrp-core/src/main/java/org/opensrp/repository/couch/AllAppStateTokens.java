package org.opensrp.repository.couch;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.UpdateConflictException;
import org.ektorp.support.GenerateView;
import org.ektorp.util.Assert;
import org.ektorp.util.Documents;
import org.motechproject.dao.MotechBaseRepository;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.AppStateToken;
import org.opensrp.repository.AppStateTokensRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.mysql.jdbc.StringUtils;

@Repository("couchAppStateTokensRepository")
@Primary
public class AllAppStateTokens extends MotechBaseRepository<AppStateToken> implements AppStateTokensRepository {
	
	private CouchDbConnector db;
	
	@Autowired
	protected AllAppStateTokens(@Qualifier(AllConstants.OPENSRP_DATABASE_CONNECTOR) CouchDbConnector db) {
		super(AppStateToken.class, db);
		this.db = db;
	}
	
	@GenerateView
	public List<AppStateToken> findByName(String name) {
		return queryView("by_name", name);
	}
	
	@GenerateView
	public List<AppStateToken> findByName(CouchDbConnector db, String name) {
		return db.queryView(createQuery("by_name").includeDocs(true).key(name), AppStateToken.class);
	}
	
	/**
	 * @throws UpdateConflictException if there was an update conflict.
	 */
	public void update(AppStateToken entity) {
		Assert.notNull(entity, "entity may not be null");
		db.update(entity);
	}
	
	/**
	 * @throws UpdateConflictException if there was an update conflict.
	 */
	public void add(AppStateToken entity) {
		add(db, entity);
	}
	
	/**
	 * @throws UpdateConflictException if there was an update conflict.
	 */
	public void add(CouchDbConnector db, AppStateToken entity) {
		Assert.notNull(entity, "entity may not be null");
		Assert.isTrue(Documents.isNew(entity), "entity must be new");
		db.create(entity);
	}
	
	/**
	 * Gets appstatetoken from the specified database
	 * 
	 * @param db
	 * @param tokenName
	 * @return AppStateToken with given name. Since model is supposed to keep track of system`s
	 *         state at any given time it throws IllegalStateException incase multiple Tokens found
	 *         with same name.
	 */
	public AppStateToken getAppStateTokenByName(CouchDbConnector db, Enum<?> tokenName) {
		List<AppStateToken> ol = findByName(db, tokenName.name());
		if (ol.size() > 1) {
			throw new IllegalStateException("System was found to have multiple token with same name (" + tokenName.name()
			        + "). This can lead to potential critical inconsistencies.");
		}
		
		return ol.size() == 0 ? null : ol.get(0);
	}
	
	public void updateAppStateToken(CouchDbConnector db, Enum<?> tokenName, Object value) {
		List<AppStateToken> ol = findByName(db, tokenName.name());
		if (ol.size() > 1) {
			throw new IllegalStateException("System was found to have multiple token with same name (" + tokenName.name()
			        + "). This can lead to potential critical inconsistencies.");
		}
		
		if (ol.size() == 0) {
			throw new IllegalStateException("Property with name (" + tokenName.name() + ") not found.");
		}
		
		AppStateToken ast = ol.get(0);
		ast.setValue(value);
		ast.setLastEditDate(System.currentTimeMillis());
		db.update(ast);
	}
	
	/**
	 * Registers a new token to manage the specified variable state (by token name) of App. The
	 * token is registered in the specified db
	 * 
	 * @param db
	 * @param tokenName
	 * @param defaultValue
	 * @param description
	 * @param suppressExceptionIfExists
	 * @return
	 */
	public AppStateToken registerAppStateToken(CouchDbConnector db, Enum<?> tokenName, Object defaultValue,
	                                           String description, boolean suppressExceptionIfExists) {
		if (tokenName == null || StringUtils.isEmptyOrWhitespaceOnly(description)) {
			throw new IllegalArgumentException("Token name and description must be provided");
		}
		
		List<AppStateToken> atl = findByName(db, tokenName.name());
		if (atl.size() > 0) {
			if (!suppressExceptionIfExists) {
				throw new IllegalArgumentException("Token with given name (" + tokenName.name() + ") already exists.");
			}
			return atl.get(0);
		}
		
		AppStateToken token = new AppStateToken(tokenName.name(), defaultValue, 0L, description);
		add(db, token);
		return token;
	}
	
}
