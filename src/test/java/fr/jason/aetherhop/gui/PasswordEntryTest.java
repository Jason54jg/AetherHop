package fr.jason.aetherhop.gui;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEntryTest {
    @Test
    void formattingHidesThePasswordWithoutChangingItsValue() throws Exception {
        EditBox field = new EditBox(null, 148, 18, Component.literal("Password"));
        field.setValue("secret-password");
        PasswordEntry.installMask(field);

        Method format = java.util.Arrays.stream(EditBox.class.getDeclaredMethods())
            .filter(method -> method.getReturnType() == FormattedCharSequence.class
                && java.util.Arrays.equals(method.getParameterTypes(), new Class<?>[]{String.class, int.class}))
            .findFirst().orElseThrow();
        format.setAccessible(true);
        FormattedCharSequence formatted = (FormattedCharSequence) format.invoke(field, field.getValue(), 0);
        StringBuilder display = new StringBuilder();
        formatted.accept((index, style, codePoint) -> { display.appendCodePoint(codePoint); return true; });
        assertEquals("***************", display.toString());
        assertEquals("secret-password", field.getValue());
    }
}
