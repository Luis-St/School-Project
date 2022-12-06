package net.vgc.client.network;

import java.util.List;
import java.util.Objects;

import net.vgc.account.LoginType;
import net.vgc.account.PlayerAccount;
import net.vgc.client.Client;
import net.vgc.client.game.action.GlobalClientActionHandler;
import net.vgc.client.player.AbstractClientPlayer;
import net.vgc.client.player.LocalPlayer;
import net.vgc.client.player.RemotePlayer;
import net.vgc.client.screen.LobbyScreen;
import net.vgc.client.screen.MenuScreen;
import net.vgc.client.screen.update.ScreenUpdateFactory;
import net.vgc.client.window.LoginWindow;
import net.vgc.game.Game;
import net.vgc.game.action.GameAction;
import net.vgc.game.action.handler.GameActionHandler;
import net.vgc.network.NetworkSide;
import net.vgc.network.packet.AbstractPacketHandler;
import net.vgc.player.GameProfile;

/**
 *
 * @author Luis-st
 *
 */

public class ClientPacketHandler extends AbstractPacketHandler {
	
	private final Client client;
	private final GlobalClientActionHandler actionHandler;
	
	public ClientPacketHandler(Client client, NetworkSide networkSide) {
		super(networkSide);
		this.client = client;
		this.actionHandler = new GlobalClientActionHandler(this.client);
	}
	
	public void handleClientLoggedIn(LoginType loginType, PlayerAccount account, boolean successful) {
		LoginWindow loginWindow = this.client.getLoginWindow();
		if (!this.client.isLoggedIn()) {
			if (successful) {
				switch (loginType) {
					case REGISTRATION: {
						LOGGER.info("Create successfully a new account");
						this.client.login(account);
						if (loginWindow != null) {
							loginWindow.handleLoggedIn(loginType);
						}
					}
						break;
					case USER_LOGIN: {
						LOGGER.debug("Successfully logged in");
						this.client.login(account);
						if (loginWindow != null) {
							loginWindow.handleLoggedIn(loginType);
						}
					}
						break;
					case GUEST_LOGIN: {
						LOGGER.debug("Successfully logged in as a guest");
						this.client.login(account);
						if (loginWindow != null) {
							loginWindow.handleLoggedIn(loginType);
						}
					}
						break;
					case UNKNOWN: {
						LOGGER.warn("Fail to log in");
					}
						break;
				}
			} else {
				LOGGER.warn("Fail to log in");
			}
		} else {
			LOGGER.warn("Fail to log in, since already logged in");
		}
	}
	
	public void handleClientLoggedOut(boolean successful) {
		LoginWindow loginWindow = this.client.getLoginWindow();
		if (successful) {
			LOGGER.info("Successfully logged out");
			this.client.logout();
			if (loginWindow != null) {
				loginWindow.handleLoggedOut();
			}
		} else {
			LOGGER.warn("Fail to log out");
		}
	}
	
	public void handleClientJoined(List<GameProfile> profiles) {
		for (GameProfile profile : profiles) {
			if (this.client.getAccount().getUUID().equals(profile.getUUID())) {
				this.client.setPlayer(new LocalPlayer(profile));
			} else {
				this.client.addRemotePlayer(new RemotePlayer(profile));
			}
		}
		this.client.setScreen(new LobbyScreen());
	}
	
	public void handlePlayerAdd(GameProfile profile) {
		ScreenUpdateFactory.onPlayerUpdatePre(profile);
		if (this.client.getAccount().getUUID().equals(profile.getUUID())) {
			if (this.client.getPlayer() == null) {
				LOGGER.warn("The local player is not set, that was not supposed to be");
				this.client.setPlayer(new LocalPlayer(profile));
			} else {
				LOGGER.warn("The local player is already set to {}, but there is another player with the same id {}", this.client.getPlayer().getProfile(), profile);
			}
		} else {
			this.client.addRemotePlayer(new RemotePlayer(profile));
		}
		ScreenUpdateFactory.onPlayerUpdatePost(profile);
	}
	
	public void handlePlayerRemove(GameProfile profile) {
		ScreenUpdateFactory.onPlayerUpdatePre(profile);
		if (this.client.getAccount().getUUID().equals(profile.getUUID())) {
			this.client.removePlayer();
		} else {
			this.client.removeRemotePlayer(new RemotePlayer(profile));
		}
		ScreenUpdateFactory.onPlayerUpdatePost(profile);
	}
	
	public void handleSyncPermission(GameProfile profile) {
		ScreenUpdateFactory.onPlayerUpdatePre(profile);
		for (AbstractClientPlayer player : this.client.getPlayers()) {
			if (player.getProfile().equals(profile)) {
				player.setAdmin(true);
				LOGGER.debug("Player {} is now a admin", player.getProfile().getName());
			} else {
				player.setAdmin(false);
			}
		}
		ScreenUpdateFactory.onPlayerUpdatePost(profile);
		LOGGER.info("Sync admins");
	}
	
	public void handleAction(GameAction<?> action) {
		GameActionHandler specificHandler = null;
		Game game = this.client.getGame();
		if (game != null) {
			specificHandler = Objects.requireNonNull(game.getActionHandler(), "The action handler of a game must not be null");
		}
		if (!action.handleType().handle(action, specificHandler, this.actionHandler)) {
			LOGGER.warn("Fail to handle a action of type {}", action.type().getName());
		}
	}
	
	public void handleServerClosed() {
		this.client.getServerHandler().close();
		this.client.removePlayer();
		this.client.setScreen(new MenuScreen());
	}
	
}
