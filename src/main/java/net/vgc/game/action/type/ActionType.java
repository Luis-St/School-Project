package net.vgc.game.action.type;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import net.luis.utils.util.ToString;
import net.vgc.game.action.Action;
import net.vgc.game.action.ActionRegistry;
import net.vgc.game.action.data.ActionData;
import net.vgc.network.Connection;
import net.vgc.network.ConnectionHandler;
import net.vgc.network.NetworkDirection;
import net.vgc.network.buffer.FriendlyByteBuffer;
import net.vgc.network.packet.Packet;
import net.vgc.network.packet.client.ClientActionPacket;
import net.vgc.network.packet.server.ServerActionPacket;

public class ActionType<T extends Action<V>, V extends ActionData> {
	
	private final String name;
	private final int id;
	private final NetworkDirection networkDirection;
	private final BiFunction<Integer, V, T> actionFactory;
	private final Function<FriendlyByteBuffer, V> dataFactory;
	
	public ActionType(String name, int id, NetworkDirection networkDirection, BiFunction<Integer, V, T> actionFactory, Function<FriendlyByteBuffer, V> dataFactory) {
		this.name = Objects.requireNonNull(StringUtils.trimToNull(name), "The name of an action type must not be null");
		this.id = id;
		this.networkDirection = Objects.requireNonNull(networkDirection, "The network direction of an action type must not be null");
		this.actionFactory = Objects.requireNonNull(actionFactory, "The factory of an action type must not be null");
		this.dataFactory = Objects.requireNonNull(dataFactory, "The data factory of an action type must not be null");
		ActionRegistry.register(this);
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getId() {
		return this.id;
	}
	
	public NetworkDirection getNetworkDirection() {
		return this.networkDirection;
	}
	
	public T create(V data) {
		return this.actionFactory.apply(this.id, data);
	}
	
	public void send(ConnectionHandler handler, V data) {
		this.send(handler.getConnection(), this.createPacket(data));
	}
	
	public void send(Connection connection, V data) {
		this.send(connection, this.createPacket(data));
	}
	
	private Packet<?> createPacket(V data) {
		if (this.networkDirection == NetworkDirection.SERVER_TO_CLIENT) {
			return new ClientActionPacket(this.create(data));
		} else if (this.networkDirection == NetworkDirection.CLIENT_TO_SERVER) {
			return new ServerActionPacket(this.create(data));
		}
		throw new RuntimeException("Can not send a packet to network side " + this.networkDirection.getTo() + " beacuse it is not a action receiver");
	}
	
	private void send(Connection connection, Packet<?> packet) {
		connection.send(packet);
	}
	
	public T decode(FriendlyByteBuffer buffer) {
		return this.create(this.dataFactory.apply(buffer));
	}
	
	@Override
	public String toString() {
		return ToString.toString(this);
	}
	
}
