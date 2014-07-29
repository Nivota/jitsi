/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.irc;

import net.java.sip.communicator.util.*;

/**
 * Some IRC-related utility methods.
 *
 * @author Danny van Heumen
 */
public final class Utils
{
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Utils.class);

    /**
     * Index indicating the end of the color code.
     */
    private static final int INDEX_END_COLOR_CODE = 3;

    /**
     * Private constructor since we do not need to construct anything.
     */
    private Utils()
    {
    }

    /**
     * Parse IRC text message and process possible control codes.
     *
     * TODO Support for color 99 (Transparent)
     *
     * @param text the message
     * @return returns the processed message or null if text message was null,
     *         since there is nothing to modify there
     */
    public static String parse(final String text)
    {
        if (text == null)
        {
            return null;
        }

        FormattedTextBuilder builder = new FormattedTextBuilder();
        for (int i = 0; i < text.length(); i++)
        {
            char val = text.charAt(i);
            switch (val)
            {
            case '\u0002':
                if (builder.isActive(ControlChar.Bold.class))
                {
                    builder.cancel(ControlChar.Bold.class, true);
                }
                else
                {
                    builder.apply(new ControlChar.Bold());
                }
                break;
            case '\u0016':
                if (builder.isActive(ControlChar.Italics.class))
                {
                    builder.cancel(ControlChar.Italics.class, true);
                }
                else
                {
                    builder.apply(new ControlChar.Italics());
                }
                break;
            case '\u001F':
                if (builder.isActive(ControlChar.Underline.class))
                {
                    builder.cancel(ControlChar.Underline.class, true);
                }
                else
                {
                    builder.apply(new ControlChar.Underline());
                }
                break;
            case '\u0003':
                Color background = null;
                // first parse foreground color code
                Color foreground = parseForegroundColor(text.substring(i + 1));
                if (foreground != null)
                {
                    i += 2;
                    background = parseBackgroundColor(text.substring(i + 1));
                    if (background != null)
                    {
                        i += INDEX_END_COLOR_CODE;
                    }
                }
                if (foreground == null && background == null)
                {
                    builder.cancel(ControlChar.ColorFormat.class, false);
                }
                else
                {
                    builder.apply(new ControlChar.ColorFormat(foreground,
                        background));
                }
                break;
            case '\u000F':
                builder.cancelAll();
                break;
            default:
                // value is a normal character, just append
                builder.append(val);
                break;
            }
        }
        return builder.done();
    }

    /**
     * Parse background color code starting with the separator.
     *
     * @param text the text starting with the background color (separator)
     * @return returns the background color
     */
    private static Color parseBackgroundColor(final String text)
    {
        try
        {
            if (text.charAt(0) == ',')
            {
                // if available, also parse background color
                int color =
                    Integer.parseInt("" + text.charAt(1) + text.charAt(2));
                color = color % Color.values().length;
                return Color.values()[color];
            }
            return null;
        }
        catch (StringIndexOutOfBoundsException e)
        {
            // Abort parsing background color. Assume only
            // foreground color available.
            LOGGER.trace("Abort parsing background color because text ended. "
                + "Assuming only foreground color was available.");
            return null;
        }
        catch (NumberFormatException e)
        {
            // No background color defined, ignoring ...
            LOGGER.trace("No background color defined. Ignoring ...");
            return null;
        }
    }

    /**
     * Parse foreground color and return corresponding Color instance.
     *
     * @param text the text to parse, starting with color code
     * @return returns Color instance
     */
    private static Color parseForegroundColor(final String text)
    {
        try
        {
            int color = Integer.parseInt("" + text.charAt(0) + text.charAt(1));
            color = color % Color.values().length;
            return Color.values()[color];
        }
        catch (StringIndexOutOfBoundsException e)
        {
            // Invalid control code, since text has ended.
            LOGGER.trace("ArrayIndexOutOfBounds during foreground "
                + "color control code parsing.");
            return null;
        }
        catch (NumberFormatException e)
        {
            LOGGER.trace("Invalid foreground color code encountered.", e);
            return null;
        }
    }

    /**
     * Format message as normal HTML-formatted message.
     *
     * @param message original IRC message
     * @return returns HTML-formatted normal message
     */
    public static String formatMessage(final String message)
    {
        return message;
    }

    /**
     * Format message as HTML-formatted notice.
     *
     * @param message original IRC message
     * @param user user nick name
     * @return returns HTML-formatted notice
     */
    public static String formatNotice(final String message, final String user)
    {
        return "<i>" + user + "</i>: " + message;
    }

    /**
     * Format message as HTML-formatted action.
     *
     * @param message original IRC message
     * @param user user nick name
     * @return returns HTML-formatted action
     */
    public static String formatAction(final String message, final String user)
    {
        return "<b>*" + user + "</b> " + message;
    }
}
