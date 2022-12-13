package net.vgc.network.packet.listener;

import static net.vgc.network.packet.listener.PacketInvokHelper.createException;
import static net.vgc.network.packet.listener.PacketInvokHelper.getInstanceObject;
import static net.vgc.network.packet.listener.PacketInvokHelper.getListeners;
import static net.vgc.network.packet.listener.PacketInvokHelper.getPacketObjects;
import static net.vgc.network.packet.listener.PacketInvokHelper.getSubscribers;
import static net.vgc.network.packet.listener.PacketInvokHelper.validateSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.luis.utils.util.ReflectionHelper;
import net.vgc.common.application.GameApplication;
import net.vgc.network.Network;
import net.vgc.network.packet.Packet;

/**
 *
 * @author Luis-st
 *
 */

public class PacketInvoker {
	
	public static void invoke(Packet<?> packet) {
		GameApplication application = Network.INSTANCE.getApplication();
		for (Class<?> clazz : getSubscribers(application.getNetworkSide())) {
			Object instanceObject = getInstanceObject(application, clazz, clazz.getAnnotation(PacketSubscriber.class));
			for (Method method : getListeners(clazz, packet)) {
				if (Modifier.isStatic(method.getModifiers())) {
					invokeStatic(application, packet, method);
				} else if (instanceObject != null) {
					if (instanceObject.getClass() == method.getDeclaringClass()) {
						invokeNonStatic(packet, instanceObject, method);
					} else {
						throw new RuntimeException("The instance class does not declare a method " + method.getDeclaringClass().getName() + "#" + method.getName());
					}
				}
			}
		}
	}
	
	private static void invokeStatic(GameApplication application, Packet<?> packet, Method method) {
		Object[] objects = getPacketObjects(application, packet, method);
		int parameterCount = method.getParameterCount();
		if (parameterCount == 0) {
			ReflectionHelper.invoke(method, null);
		} else if (parameterCount == 1) {
			if (validateSignature(method, application)) {
				ReflectionHelper.invoke(method, null, application);
			} else {
				throw createException(method, application);
			}
		} else if (parameterCount == 2 && method.getParameterTypes()[1].isInstance(packet)) {
			if (validateSignature(method, application, packet)) {
				ReflectionHelper.invoke(method, null, application, packet);
			} else {
				throw createException(method, application, packet);
			}
		} else if (method.getAnnotation(PacketListener.class).value() == packet.getClass()) {
			if (validateSignature(method, objects)) {
				ReflectionHelper.invoke(method, null, objects);
			} else {
				throw createException(method, objects);
			}
		}
	}
	
	private static void invokeNonStatic(Packet<?> packet, Object instanceObject, Method method) {
		Object[] objects = getPacketObjects(null, packet, method);
		int parameterCount = method.getParameterCount();
		if (parameterCount == 0) {
			ReflectionHelper.invoke(method, instanceObject);
			
		} else if (parameterCount == 1 && method.getParameterTypes()[0].isInstance(packet)) {
			if (validateSignature(method, packet)) {
				ReflectionHelper.invoke(method, instanceObject, packet);
			} else {
				throw createException(method, packet);
			}
		} else if (method.getAnnotation(PacketListener.class).value() == packet.getClass()) {
			if (validateSignature(method, objects)) {
				ReflectionHelper.invoke(method, instanceObject, objects);
			} else {
				throw createException(method, objects);
			}
		}
	}
	
}
