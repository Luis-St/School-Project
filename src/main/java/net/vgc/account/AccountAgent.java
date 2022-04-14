package net.vgc.account;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.vgc.common.InfoResult;
import net.vgc.common.Result;
import net.vgc.language.Languages;
import net.vgc.language.TranslationKey;

public final class AccountAgent {
	
	protected static final Logger LOGGER = LogManager.getLogger();
	
	protected final List<PlayerAccount> accounts;
	
	public AccountAgent(List<PlayerAccount> accounts) {
		this.accounts = accounts;
	}
	
	public List<PlayerAccount> getAccounts() {
		return this.accounts;
	}
	
	public boolean isPresent(UUID uuid) {
		return this.accounts.stream().map(PlayerAccount::getUUID).collect(Collectors.toList()).contains(uuid);
	}
	
	public PlayerAccount getAccount(UUID uuid) {
		if (this.isPresent(uuid)) {
			for (PlayerAccount account : this.accounts) {
				if (account.getUUID().equals(uuid)) {
					return account;
				}
			}
		}
		return PlayerAccount.UNKNOWN;
	}
	
	public boolean removeAccount(UUID uuid) {
		if (this.isPresent(uuid)) {
			PlayerAccount account = this.getAccount(uuid);
			LOGGER.info("Remove account: {}", account);
			return this.accounts.remove(account);
		}
		LOGGER.warn("Fail to remove account with uuid {}, since it does not exists", uuid);
		return false;
	}
	
	public void setTaken(UUID uuid, boolean taken) {
		if (this.isPresent(uuid)) {
			this.getAccount(uuid).setTaken(taken);
		}
	}
	
	protected String checkOrGeneratePassword(String password, int length) {
		if (password != null && password.trim().isEmpty()) {
			return password;
		}
		Random rng = new Random(System.currentTimeMillis());
		String characters = "~_+-!#$%&0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		password = "";
		for (int i = 0; i < length; i++) {
			password += characters.charAt(rng.nextInt(characters.length()));
		}
		return password;
	}
	
	protected UUID generateUUID(String name, String password) {
		Random rng = new Random(name.charAt(0) * password.charAt(0));
		long mostBits = 0;
		String most = name + password + name;
		for (int i = 0; i < most.length(); i++) {
			mostBits += most.charAt(i) * rng.nextInt(Math.max(1, i * i) * Math.max(1, i * i)) * rng.nextInt(Math.max(1, i * i) * Math.max(1, i * i));
		}
		long leastBits = 0;
		String least = password + name + password;
		for (int i = 0; i < least.length(); i++) {
			leastBits += least.charAt(i) * rng.nextInt(Math.max(1, i * i) * Math.max(1, i * i)) * rng.nextInt(Math.max(1, i * i) * Math.max(1, i * i));
		}
		return new UUID(mostBits + rng.nextLong(Math.max(1, leastBits)), leastBits + rng.nextLong(Math.max(1, mostBits)));
	}
	
	public PlayerAccount createAccount(String name, String password, boolean guest) {
		password = this.checkOrGeneratePassword(password, 15);
		PlayerAccount account = new PlayerAccount(name, password, this.generateUUID(name, password), guest);
		this.accounts.add(account);
		AccountServer.getInstance().refresh();
		return account;
	}
	
	public PlayerAccount createAndLogin(String name, String password, boolean guest) {
		PlayerAccount account = this.createAccount(name, password, guest);
		account.setTaken(true);
		return account;
	}
	
	public PlayerAccountInfo accountLogin(String name, String password) {
		UUID uuid = this.generateUUID(name, password);
		if (this.isPresent(uuid)) {
			PlayerAccount account = this.getAccount(uuid);
			if (account.isTaken()) {
				LOGGER.warn("Fail to login, since the account {} is already used by another player", account.toString().replace("PlayerAccount", ""));
				return new PlayerAccountInfo(new InfoResult(Result.FAILED, TranslationKey.createAndGet(Languages.EN_US, "account.login.taken")), PlayerAccount.UNKNOWN);
			} else {
				LOGGER.info("Client logged in with account: {}", account);
				this.setTaken(uuid, true);
				return new PlayerAccountInfo(new InfoResult(Result.SUCCESS, TranslationKey.createAndGet(Languages.EN_US, "account.login.successfully")), account);
			}
		}
		LOGGER.warn("Fail to login, since there is no account with uuid {} and account data: name {} password {}", uuid, name, password);
		return new PlayerAccountInfo(new InfoResult(Result.FAILED, TranslationKey.createAndGet(Languages.EN_US, "account.login.no")), PlayerAccount.UNKNOWN);
	}
	
	public InfoResult accountLogout(String name, String password) {
		UUID uuid = this.generateUUID(name, password);
		if (this.isPresent(uuid)) {
			if (this.getAccount(uuid).isTaken()) {
				LOGGER.info("Client logged out with account: {}", this.getAccount(uuid));
				this.setTaken(uuid, false);
				return new InfoResult(Result.SUCCESS, TranslationKey.createAndGet(Languages.EN_US, "account.logout.successfully"));
			} else {
				LOGGER.warn("Fail to logout, since the account is not used by a player");
				return new InfoResult(Result.FAILED, TranslationKey.createAndGet(Languages.EN_US, "account.logout.unused"));
			}
		}
		LOGGER.warn("Fail to logout, since there is no account with uuid {} and account data: name {} password {}", uuid, name, password);
		return new InfoResult(Result.FAILED, TranslationKey.createAndGet(Languages.EN_US, "account.login.no"));
	}
	
	public void close() {
		this.accounts.clear();
	}

}
