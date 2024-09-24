import java.nio.file.Files
import java.nio.file.Path
import java.text.DecimalFormat
import java.io.BufferedReader
import java.io.BufferedWriter

// Función para leer el archivo CSV y devolver un mapa con los datos organizados por columnas
fun leerCotizaciones(rutaFichero: Path): Map<String, List<Double>> {
    // Mapa mutable para almacenar las listas de valores correspondientes a cada columna
    val cotizaciones = mutableMapOf<String, MutableList<Double>>()

    // Abrimos el archivo usando Files.newBufferedReader para crear un BufferedReader
    val br: BufferedReader = Files.newBufferedReader(rutaFichero)

    // Usamos el bloque .use para asegurarnos de que el BufferedReader se cierre automáticamente
    br.use { reader ->
        // Leer la primera línea del archivo que contiene las cabeceras (Nombres de las columnas)
        val headers = reader.readLine().split(";").drop(1) // Saltamos la primera columna "Nombre"

        // Inicializamos listas vacías para cada una de las columnas (Final, Máximo, Mínimo, Volumen, Efectivo)
        headers.forEach { header ->
            cotizaciones[header] = mutableListOf() // Se agrega una lista vacía para cada cabecera
        }

        // Leemos línea por línea el archivo a partir de la segunda línea (los datos)
        reader.forEachLine { line ->
            // Dividimos cada línea en columnas usando ";" como delimitador
            val columns = line.split(";")

            // Iteramos sobre las columnas, comenzando desde la columna 1 (saltamos el nombre)
            for (i in 1 until columns.size) {
                // Reemplazamos las comas con puntos para convertir los valores a Double correctamente
                val value = columns[i].replace(",", ".").toDoubleOrNull()

                // Añadimos el valor convertido a la lista correspondiente a la columna en el mapa
                value?.let {
                    cotizaciones[headers[i - 1]]?.add(it) // headers[i-1] mapea correctamente las columnas
                }
            }
        }
    }

    // Retornamos el mapa con los datos organizados por columnas
    return cotizaciones
}

// Función para calcular las estadísticas (mínimo, máximo, media) y escribir un nuevo archivo CSV con estos datos
fun procesarEstadisticas(cotizaciones: Map<String, List<Double>>, rutaSalida: Path) {
    // Creamos o abrimos el archivo de salida usando Files.newBufferedWriter
    val bw: BufferedWriter = Files.newBufferedWriter(rutaSalida)

    // Usamos el bloque .use para asegurarnos de que el BufferedWriter se cierre automáticamente
    bw.use { writer ->
        // Escribimos la cabecera del nuevo archivo CSV
        writer.write("Columna;Minimo;Maximo;Media\n")

        // Para cada columna en el mapa de cotizaciones, calculamos las estadísticas
        for ((columna, valores) in cotizaciones) {
            // Calculamos el valor mínimo, máximo y la media de la lista de valores
            val minimo = valores.minOrNull() ?: 0.0
            val maximo = valores.maxOrNull() ?: 0.0
            val media = if (valores.isNotEmpty()) valores.average() else 0.0

            // Usamos DecimalFormat para formatear los resultados a dos decimales
            val df = DecimalFormat("#.##")

            // Escribimos los resultados (mínimo, máximo, media) en el archivo CSV
            writer.write("${columna};${df.format(minimo)};${df.format(maximo)};${df.format(media)}\n")
        }
    }
}

fun main() {
    // Definimos la ruta raíz donde se encuentran los archivos
    val rutaRaiz = Path.of("src")

    // Definimos la ruta del archivo de entrada (cotizacion.csv)
    val rutaEntrada = rutaRaiz.resolve("main")
        .resolve("resources")
        .resolve("ficheros")
        .resolve("cotizacion.csv")

    // Definimos la ruta del archivo de salida (estadisticas.csv)
    val rutaSalida = rutaRaiz.resolve("main")
        .resolve("resources")
        .resolve("ficheros")
        .resolve("estadisticas.csv")

    // Llamamos a la función para leer las cotizaciones desde el archivo de entrada
    val cotizaciones = leerCotizaciones(rutaEntrada)

    // Llamamos a la función para procesar las estadísticas y generar el archivo de salida
    procesarEstadisticas(cotizaciones, rutaSalida)

    // Informamos que el archivo de estadísticas ha sido generado correctamente
    println("Fichero de estadísticas generado: $rutaSalida")
}