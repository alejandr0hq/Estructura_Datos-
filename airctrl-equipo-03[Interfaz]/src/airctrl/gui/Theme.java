package airctrl.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Paleta y tipografia de AIRCTRL, inspirada en las tiras de progreso de vuelo de una torre de control. */
public final class Theme {
    public static final Color NAVY = new Color(0x14213D);
    public static final Color NAVY_HOVER = new Color(0x1E2F55);
    public static final Color NAVY_SELECTED = new Color(0x263B69);
    public static final Color NAVY_TEXT = new Color(0xC3CDE0);
    public static final Color NAVY_MUTED = new Color(0x7F8DAA);

    public static final Color BG = new Color(0xEEF1F4);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_ALT = new Color(0xF6F8FA);
    public static final Color LINE = new Color(0xD8DEE6);
    public static final Color INK = new Color(0x1B2430);
    public static final Color MUTED = new Color(0x5D6B7E);
    public static final Color ACCENT = new Color(0x2B6CB0);
    public static final Color ACCENT_DARK = new Color(0x1F5494);
    public static final Color SELECTION = new Color(0xDCE7F5);

    public static final Color OK = new Color(0x2B8A6E);
    public static final Color WARN = new Color(0xC98A0B);
    public static final Color DANGER = new Color(0xC8372D);

    private static final Color[] PRIORITY = {
            new Color(0x7A8594), // 1 normal
            new Color(0x2B8A6E), // 2 importante
            new Color(0xD9A21B), // 3 retraso
            new Color(0xE0662D), // 4 incidente grave
            new Color(0xC8372D)  // 5 emergencia
    };
    private static final String[] PRIORITY_NAMES = {
            "Normal", "Operacion importante", "Retraso", "Incidente grave", "Emergencia"
    };

    public static final String SANS = pick("Inter", "Segoe UI", "SF Pro Text", ".AppleSystemUIFont",
            "Helvetica Neue", "Ubuntu", "Cantarell", "Noto Sans", "DejaVu Sans", "SansSerif");
    public static final String MONO = pick("JetBrains Mono", "SF Mono", "Menlo", "Consolas",
            "Ubuntu Mono", "DejaVu Sans Mono", "Monospaced");

    public static final Font BODY = new Font(SANS, Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font(SANS, Font.BOLD, 13);
    public static final Font SMALL = new Font(SANS, Font.PLAIN, 12);
    public static final Font SMALL_BOLD = new Font(SANS, Font.BOLD, 12);
    public static final Font H1 = new Font(SANS, Font.BOLD, 22);
    public static final Font H2 = new Font(SANS, Font.BOLD, 15);
    public static final Font METRIC = new Font(SANS, Font.BOLD, 26);
    public static final Font CODE = new Font(MONO, Font.BOLD, 14);
    public static final Font CODE_SMALL = new Font(MONO, Font.PLAIN, 12);
    public static final Font CODE_LARGE = new Font(MONO, Font.BOLD, 18);

    private Theme() {
    }

    public static Color priority(int value) {
        return PRIORITY[Math.max(1, Math.min(5, value)) - 1];
    }

    public static String priorityName(int value) {
        return PRIORITY_NAMES[Math.max(1, Math.min(5, value)) - 1];
    }

    /** Color semantico para cualquier estado de vuelo, puerta, equipaje o aeronave. */
    public static Color status(String status) {
        if (status == null) {
            return MUTED;
        }
        return switch (status) {
            case "AVAILABLE", "BOARDING", "DELIVERED", "ACTIVE", "RESUELTO" -> OK;
            case "OCCUPIED", "LOADED", "SCHEDULED" -> ACCENT;
            case "MAINTENANCE", "DELAYED", "IN_TRANSIT", "ABIERTO" -> WARN;
            case "EMERGENCY", "MISSING" -> DANGER;
            case "CLOSED", "CANCELLED" -> new Color(0x4A5566);
            default -> MUTED; // DEPARTED, LANDED, CHECKED
        };
    }

    /** Etiqueta en espanol para mostrar en pantalla; el valor guardado sigue en ingles como en los CSV. */
    public static String statusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "Sin dato";
        }
        return switch (status) {
            case "AVAILABLE" -> "Disponible";
            case "OCCUPIED" -> "Ocupada";
            case "MAINTENANCE" -> "Mantenimiento";
            case "CLOSED" -> "Cerrada";
            case "SCHEDULED" -> "Programado";
            case "BOARDING" -> "Abordando";
            case "DELAYED" -> "Retrasado";
            case "EMERGENCY" -> "Emergencia";
            case "DEPARTED" -> "Despego";
            case "LANDED" -> "Aterrizo";
            case "CANCELLED" -> "Cancelado";
            case "CHECKED" -> "Documentado";
            case "LOADED" -> "Cargado";
            case "IN_TRANSIT" -> "En traslado";
            case "DELIVERED" -> "Entregado";
            case "MISSING" -> "Extraviado";
            case "ACTIVE" -> "Activa";
            case "ABIERTO" -> "Abierto";
            case "RESUELTO" -> "Resuelto";
            default -> status;
        };
    }

    public static Color tint(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color mix(Color a, Color b, double t) {
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static String pick(String... candidates) {
        Set<String> available;
        try {
            available = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        } catch (Throwable error) {
            available = Set.of();
        }
        for (String candidate : candidates) {
            if (available.contains(candidate)) {
                return candidate;
            }
        }
        return candidates[candidates.length - 1];
    }
}
