package net.vgc.client.network;

import java.util.List;
import java.util.stream.Collectors;

import net.vgc.account.LoginType;
import net.vgc.account.PlayerAccount;
import net.vgc.client.Client;
import net.vgc.client.player.AbstractClientPlayer;
import net.vgc.client.player.LocalPlayer;
import net.vgc.client.player.RemotePlayer;
import net.vgc.client.screen.LobbyScreen;
import net.vgc.client.screen.MenuScreen;
import net.vgc.client.window.LoginWindow;
import net.vgc.network.NetworkSide;
import net.vgc.network.packet.AbstractPacketListener;
import net.vgc.player.GameProfile;

public class ClientPacketListener extends AbstractPacketListener {
	
	protected final Client client;
	
	public ClientPacketListener(Client client, NetworkSide networkSide) {
		super(networkSide);
		this.client = client;
	}
	
	public void handleClientLoggedIn(LoginType loginType, PlayerAccount account, boolean successful) {
		this.checkSide();
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
					} break;
					case USER_LOGIN: {
						LOGGER.debug("Successfully logged in");
						this.client.login(account);
						if (loginWindow != null) {
							loginWindow.handleLoggedIn(loginType);
						}
					} break;
					case GUEST_LOGIN: {
						LOGGER.debug("Successfully logged in as a guest");
						this.client.login(account);
						if (loginWindow != null) {
							loginWindow.handleLoggedIn(loginType);
						}
					} break;
					case UNKNOWN: {
						LOGGER.warn("Fail to log in");
					} break;
				}
			} else {
				LOGGER.warn("Fail to log in");
			}
		} else {
			LOGGER.warn("Fail to log in, since already logged in");
		}
	}
	
	public void handleClientLoggedOut(boolean successful) {
		this.checkSide();
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
	
	public void handleClientJoined(List<GameProfile> gameProfiles) {
		this.checkSide();
		for (GameProfile gameProfile : gameProfiles) {
			if (this.client.getAccount().getUUID().equals(gameProfile.getUUID())) {
				this.client.setPlayer(new LocalPlayer(gameProfile));
			} else {
				this.client.addRemotePlayer(new RemotePlayer(gameProfile));
			}
		}
		this.client.setScreen(new LobbyScreen());
	}
	
	public void handleClientPlayerAdd(GameProfile gameProfile) {
		this.checkSide();
		if (this.client.getAccount().getUUID().equals(gameProfile.getUUID())) {
			if (this.client.getPlayer() == null) {
				LOGGER.warn("The local player is not set, that was not supposed to be");
				this.client.setPlayer(new LocalPlayer(gameProfile));
			} else {
				LOGGER.warn("The local player is already set to {}, but there is another player with the same id {}", this.client.getPlayer().getGameProfile(), gameProfile);
			}
		} else {
			this.client.addRemotePlayer(new RemotePlayer(gameProfile));
		}
	}
	
	public void handleClientPlayerRemove(GameProfile gameProfile) {
		this.checkSide();
		if (this.client.getAccount().getUUID().equals(gameProfile.getUUID())) {
			this.client.removePlayer();
		} else {
			this.client.removeRemotePlayer(new RemotePlayer(gameProfile));
		}
	}
	
	public void handleSyncPermission(GameProfile gameProfile) {
		this.checkSide();
		for (AbstractClientPlayer player : this.client.getPlayers()) {
			if (player.getGameProfile().equals(gameProfile)) {
				player.setAdmin(true);
			} else {
				player.setAdmin(false);
			}
		}
		LOGGER.info("Admins {}", this.client.getPlayers().stream().map(AbstractClientPlayer::isAdmin).collect(Collectors.toList()));
	}
	
	public void handleServerClosed() {
		this.client.getServerHandler().close();
		this.client.removePlayer();
		this.client.setScreen(new MenuScreen());
	}
	
}
