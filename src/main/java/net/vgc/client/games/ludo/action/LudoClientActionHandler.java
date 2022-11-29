package net.vgc.client.games.ludo.action;

import net.vgc.game.Game;
import net.vgc.game.action.Action;
import net.vgc.game.action.data.specific.FieldInfoData;
import net.vgc.game.action.handler.AbstractActionHandler;
import net.vgc.game.action.type.ActionTypes;
import net.vgc.game.map.field.GameField;
import net.vgc.game.map.field.GameFieldInfo;
import net.vgc.game.map.field.GameFieldPos;
import net.vgc.game.player.GamePlayer;
import net.vgc.game.player.figure.GameFigure;
import net.vgc.player.GameProfile;

/**
 *
 * @author Luis-st
 *
 */

public class LudoClientActionHandler extends AbstractActionHandler {
	
	public LudoClientActionHandler(Game game) {
		super(game);
	}
	
	@Override
	public void handle(Action<?> action) {
		if (action.type() == ActionTypes.UPDATE_MAP && action.data() instanceof FieldInfoData data) {
			this.handleUpdateMap(data);
		}
	}
	
	private void handleUpdateMap(FieldInfoData data) {
		for (GameFieldInfo fieldInfo : data.getFieldInfos()) {
			GameProfile profile = fieldInfo.getProfile();
			GameFieldPos fieldPos = fieldInfo.getFieldPos();
			GameField field = this.getMap().getField(fieldInfo.getFieldType(), fieldInfo.getPlayerType(), fieldPos);
			if (field == null) {
				LOGGER.warn("Fail to update game field, since there is no field for pos {}", fieldPos.getPosition());
				continue;
			} else {
				if (field.isShadowed()) {
					field.setShadowed(false);
				}
				if (profile.equals(GameProfile.EMPTY)) {
					field.setFigure(null);
					continue;
				}
				GamePlayer player = this.getGame().getPlayerFor(profile);
				if (player == null) {
					LOGGER.warn("Fail to place a figure of player {} at field {}, since the player does not exsists", profile.getName(), fieldPos.getPosition());
					continue;
				}
				GameFigure figure = player.getFigure(fieldInfo.getFigureCount());
				if (!figure.getUUID().equals(fieldInfo.getFigureUUID())) {
					LOGGER.warn("Fail to place figure {} of player {} at field {}, since the figure uuid {} does not match with the server on {}", figure.getCount(), profile.getName(), fieldPos.getPosition(), figure.getUUID(), fieldInfo.getFigureUUID());
					continue;
				} else {
					field.setFigure(figure);
				}
			}
		}
	}
	
}
