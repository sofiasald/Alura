
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConversorMonedas {

    private static Scanner scanner = new Scanner(System.in);

    public static Scanner getScanner() {
        return scanner;
    }

    public static void setScanner(Scanner scanner) {
        ConversorMonedas.scanner = scanner;
    }
    
    // Clase para manejar las conexiones con la API de Exchange Rate
    public static class ExchangeRateAPI {
        private static final String API_KEY = "370cb1ba0776d4752774e241"; 
        private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
        

    
        public static double obtenerTasaCambio(String monedaBase, String monedaDestino) {
            try {
                // Construir URL para la API
                String urlString = BASE_URL + API_KEY + "/pair/" + monedaBase + "/" + monedaDestino;
                java.net.URI uri = java.net.URI.create(urlString);
                URL url = uri.toURL();
                
                // Establecer conexión HTTP
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                
                // Verificar código de respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("❌ Error en la API: Código " + responseCode);
                    return -1;
                }
                
                // Leer respuesta
                StringBuilder response = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                )) {
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                // Parsear JSON y obtener tasa de cambio
                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                
                if (jsonObject.get("result").getAsString().equals("success")) {
                    return jsonObject.get("conversion_rate").getAsDouble();
                } else {
                    System.out.println("❌ Error en la respuesta de la API");
                    return -1;
                }
                
            } catch (java.net.MalformedURLException e) {
                System.out.println("❌ URL mal formada: " + e.getMessage());
                return -1;
            } catch (java.io.IOException e) {
                System.out.println("❌ Error de entrada/salida al conectar con la API: " + e.getMessage());
                return -1;
            }
            }
        }
        
        /**
         * Convierte una cantidad de una moneda a otra usando la API
         * @param monedaBase Código de la moneda base
         * @param monedaDestino Código de la moneda destino
         * @param cantidad Cantidad a convertir
         * @return Cantidad convertida, o -1 si hay error
         */
        public static double convertirMoneda(String monedaBase, String monedaDestino, double cantidad) {
            try {
                String urlString = ExchangeRateAPI.BASE_URL + ExchangeRateAPI.API_KEY + "/pair/" + monedaBase + "/" + monedaDestino + "/" + cantidad;
                URL url = new URL(urlString);
                
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                
                if (connection.getResponseCode() != 200) {
                    return -1;
                }
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                
                if (jsonObject.get("result").getAsString().equals("success")) {
                    return jsonObject.get("conversion_result").getAsDouble();
                } else {
                    return -1;
                }
                
            } catch (java.net.MalformedURLException e) {
                System.out.println("❌ URL mal formada: " + e.getMessage());
                return -1;
            } catch (java.io.IOException e) {
                System.out.println("❌ Error de entrada/salida al conectar con la API: " + e.getMessage());
                return -1;
            }
        }
    }
    
    // Clase alternativa para usar sin API key (modo offline/demo)
    private static class TasasCambioLocal {
        // Tasas de cambio de respaldo (actualizar manualmente según sea necesario)
        private static final double USD_TO_ARS = 811.75;
        private static final double USD_TO_BRL = 4.92;
        private static final double USD_TO_COP = 4100.0;
        
        public static double convertir(String monedaOrigen, String monedaDestino, double cantidad) {
            double tasa = obtenerTasa(monedaOrigen, monedaDestino);
            return cantidad * tasa;
        }
        
        private static double obtenerTasa(String origen, String destino) {
            // Conversiones desde USD
            if (origen.equals("USD") && destino.equals("ARS")) return USD_TO_ARS;
            if (origen.equals("USD") && destino.equals("BRL")) return USD_TO_BRL;
            if (origen.equals("USD") && destino.equals("COP")) return USD_TO_COP;
            
            // Conversiones hacia USD
            if (origen.equals("ARS") && destino.equals("USD")) return 1.0 / USD_TO_ARS;
            if (origen.equals("BRL") && destino.equals("USD")) return 1.0 / USD_TO_BRL;
            if (origen.equals("COP") && destino.equals("USD")) return 1.0 / USD_TO_COP;
            
            return 1.0; // Mismo tipo de moneda
        }
    }
    
    private static boolean usarAPI = true; // Cambiar a false para usar tasas locales
    
    public static void main(String[] args) {
        mostrarBienvenida();
        verificarConexionAPI();
        
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            int opcion = obtenerOpcion();
            
            switch (opcion) {
                case 1:
                    convertirMoneda("USD", "ARS", "Dólar", "Peso argentino");
                    break;
                case 2:
                    convertirMoneda("ARS", "USD", "Peso argentino", "Dólar");
                    break;
                case 3:
                    convertirMoneda("USD", "BRL", "Dólar", "Real brasileño");
                    break;
                case 4:
                    convertirMoneda("BRL", "USD", "Real brasileño", "Dólar");
                    break;
                case 5:
                    convertirMoneda("USD", "COP", "Dólar", "Peso colombiano");
                    break;
                case 6:
                    convertirMoneda("COP", "USD", "Peso colombiano", "Dólar");
                    break;
                case 7:
                    continuar = false;
                    mostrarDespedida();
                    break;
                default:
                    System.out.println("❌ Opción no válida. Por favor, selecciona una opción del 1 al 7.");
            }
            
            if (continuar) {
                presionarEnterParaContinuar();
            }
        }
        
        scanner.close();
    }
    
    private static void verificarConexionAPI() {
        System.out.print("🔄 Verificando conexión con Exchange Rate API...");
        
        if (ExchangeRateAPI.API_KEY.equals("TU_API_KEY_AQUI")) {
            System.out.println("\n⚠️  API Key no configurada. Usando tasas locales.");
            usarAPI = false;
        } else {
            // Probar conexión con la API
            double testRate = ExchangeRateAPI.obtenerTasaCambio("USD", "EUR");
            if (testRate > 0) {
                System.out.println(" ✅ Conectado!");
                usarAPI = true;
            } else {
                System.out.println("\n❌ No se pudo conectar. Usando tasas locales.");
                usarAPI = false;
            }
        }
        
        System.out.println("📊 Fuente de datos: " + (usarAPI ? "Exchange Rate API (Tiempo Real)" : "Tasas Locales"));
        System.out.println();
    }
    
    private static void mostrarBienvenida() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              ¡BIENVENIDO AL CONVERSOR DE MONEDAS!         ║");
        System.out.println("║                                                            ║");
        System.out.println("║          Convierte fácilmente entre diferentes            ║");
        System.out.println("║              monedas con tasas actualizadas               ║");
        System.out.println("║                                                            ║");
        System.out.println("║            🌐 Powered by Exchange Rate API                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    private static void mostrarMenu() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│                    MENÚ DE CONVERSIÓN                  │");
        System.out.println("├─────────────────────────────────────────────────────────┤");
        System.out.println("│  1) Dólar             ==> Peso argentino               │");
        System.out.println("│  2) Peso argentino    ==> Dólar                        │");
        System.out.println("│  3) Dólar             ==> Real brasileño               │");
        System.out.println("│  4) Real brasileño    ==> Dólar                        │");
        System.out.println("│  5) Dólar             ==> Peso colombiano              │");
        System.out.println("│  6) Peso colombiano   ==> Dólar                        │");
        System.out.println("│  7) Salir                                               │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.print("Elije una opción válida: ");
    }
    
    private static int obtenerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static void convertirMoneda(String codigoOrigen, String codigoDestino, 
                                       String nombreOrigen, String nombreDestino) {
        System.out.println("\n💱 Conversión de " + nombreOrigen + " a " + nombreDestino);
        System.out.println("─".repeat(50));
        
        System.out.print("Ingresa el valor que deseas convertir: ");
        
        try {
            double cantidad = Double.parseDouble(scanner.nextLine());
            
            if (cantidad < 0) {
                System.out.println("❌ El valor debe ser positivo.");
                return;
            }
            
            System.out.print("🔄 Obteniendo tasa de cambio actual...");
            
            double resultado;
            if (usarAPI) {
                resultado = ExchangeRateAPI.convertirMoneda(codigoOrigen, codigoDestino, cantidad);
                if (resultado == -1) {
                    System.out.println("\n⚠️  Error con la API. Usando tasas locales.");
                    resultado = TasasCambioLocal.convertir(codigoOrigen, codigoDestino, cantidad);
                }
            } else {
                resultado = TasasCambioLocal.convertir(codigoOrigen, codigoDestino, cantidad);
            }
            
            System.out.println(" ✅ Completado!");
            System.out.println("\n✅ RESULTADO DE LA CONVERSIÓN:");
            System.out.printf("   El valor %.2f [%s] corresponde al valor final de =>>> %.2f [%s]%n", 
                            cantidad, nombreOrigen, resultado, nombreDestino);
            
            // Mostrar tasa de cambio utilizada
            double tasa = resultado / cantidad;
            System.out.printf("   📊 Tasa de cambio aplicada: 1 %s = %.4f %s%n", 
                            codigoOrigen, tasa, codigoDestino);
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Por favor, ingresa un número válido.");
        }
    }
    
    private static void presionarEnterParaContinuar() {
        System.out.println("\n" + "─".repeat(60));
        System.out.print("Presiona Enter para continuar...");
        scanner.nextLine();
        System.out.println();
    }
    
    private static void mostrarDespedida() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ¡GRACIAS POR USAR                      ║");
        System.out.println("║                EL CONVERSOR DE MONEDAS!                   ║");
        System.out.println("║                                                            ║");
        System.out.println("║         🌐 Con datos actualizados en tiempo real          ║");
        System.out.println("║              ¡Que tengas un excelente día!                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}


