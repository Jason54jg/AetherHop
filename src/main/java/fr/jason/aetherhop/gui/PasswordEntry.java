package fr.jason.aetherhop.gui;

import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Masks the display while leaving editing and the saved password unchanged. */
final class PasswordEntry extends StringListEntry {
    PasswordEntry(Component label, String value, Component resetLabel, Consumer<String> save) {
        super(label, value, resetLabel, () -> "", save);
        installMask(textFieldWidget);
    }

    static void installMask(EditBox field) {
        // 1.21-1.21.8 use setFormatter(BiFunction); later releases use addFormatter(TextFormatter).
        // One jar covers both APIs. Discover the setter by its parameter contract so this also
        // works after Fabric remaps method/class names, without depending on intermediary IDs.
        for (Method setter : EditBox.class.getMethods()) {
            if (setter.getParameterCount() != 1 || setter.getReturnType() != void.class) continue;
            Class<?> parameter = setter.getParameterTypes()[0];
            Object formatter = null;
            if (parameter == BiFunction.class) {
                formatter = (BiFunction<String, Integer, FormattedCharSequence>) (text, offset) -> mask(text);
            } else if (parameter.isInterface()) {
                for (Method method : parameter.getMethods()) {
                    if (method.getReturnType() == FormattedCharSequence.class
                        && java.util.Arrays.equals(method.getParameterTypes(), new Class<?>[]{String.class, int.class})) {
                        formatter = Proxy.newProxyInstance(parameter.getClassLoader(), new Class<?>[]{parameter},
                            (proxy, invoked, args) -> {
                                if (invoked.getDeclaringClass() != Object.class) return mask((String) args[0]);
                                return switch (invoked.getName()) {
                                    case "equals" -> proxy == args[0];
                                    case "hashCode" -> System.identityHashCode(proxy);
                                    default -> "AetherHop password formatter";
                                };
                            });
                        break;
                    }
                }
            }
            if (formatter != null) {
                try {
                    setter.invoke(field, formatter);
                    return;
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Could not mask the proxy password", e);
                }
            }
        }
        throw new IllegalStateException("No supported password formatter was found");
    }

    private static FormattedCharSequence mask(String text) {
        return FormattedCharSequence.forward("*".repeat(text.length()), Style.EMPTY);
    }
}
