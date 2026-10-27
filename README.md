# AgroCampo - examen Axel Perez 2

Aplicación Android del caso AgroSierra, adaptada desde [PharmaMobil de dreyna](https://github.com/dreyna/pharmaMobil). Se conserva el enfoque de módulos Android/shared, Compose, MVVM y Koin; se reemplaza el caso farmacéutico y los repositorios en memoria por REST y SQLDelight.

## Abrir y ejecutar

1. Inicia AgroAPI con el comando de su README.
2. Abre esta carpeta en Android Studio. Usa JDK 21, instala SDK Android 36 y configura `local.properties` con `sdk.dir=RUTA_DE_TU_SDK` (Android Studio lo genera).
3. Selecciona un emulador Android y ejecuta `androidApp`.

Compilar por consola: `.\gradlew.bat :androidApp:assembleDebug`. Pruebas compartidas en JVM: `.\gradlew.bat :shared:jvmTest`. En Linux/macOS usa `sh gradlew`.

La firma de depuración se genera automáticamente en `debug.keystore`; no es una clave de publicación. El APK se genera en `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. El paquete incluye un APK ya compilado en `binarios/`.

La URL se centraliza en `data/remote/ClienteHttp.kt`, `ApiConfig`. `10.0.2.2` funciona desde el emulador estándar. Para un dispositivo físico configura una dirección HTTPS alcanzable y revisa el certificado; la excepción HTTP local solo permite `10.0.2.2` y `localhost`.

Usuarios de prueba: `tecnico1`, `jefe1`, `admin`, todos con contraseña `Agro2026*`. El primer login y la descarga inicial requieren red. Después la sesión vigente y los datos locales permiten trabajar sin conexión. Una sesión vencida requiere autenticarse de nuevo.

## Arquitectura

`shared/src/commonMain/kotlin/pe/upeu/agrosierra/domain`: modelos sin anotaciones de JSON, errores sellados, contratos y casos de uso.

`data/remote`: un HttpClient, URL base, DTO, AgroApi y traducción de errores. Se configura serialización tolerante a campos nuevos, timeout y logs sin Authorization. Un único `defaultRequest` añade Bearer a las rutas protegidas.

`data/local`: interfaz AlmacenSeguro y BaseLocal con consultas observables. `shared/src/commonMain/sqldelight/.../AgroDb.sq` contiene tablas y SQL. `data/repository/AgroRepositorioOffline.kt` coordina cola y sincronización.

`presentation`: ViewModel, StateFlow, estados de carga/contenido/vacío/error, login, padrón, detalle, formulario y perfil. `di`: módulos Koin. `androidApp/...`: driver SQLite, Keystore AES-GCM, observador de conectividad e inicio de Koin.

## Offline-first

La pantalla observa exclusivamente `BaseLocal.lotes()` y `BaseLocal.labores()` mediante Flow. Un POST nunca alimenta directamente una lista de pantalla: confirma la fila local y el Flow vuelve a emitir.

Una nueva labor usa `minOf(minId, 0) - 1`. Se guarda con `pendiente=1`, operación CREAR y una clave de idempotencia estable. Al enviarla, el servidor asigna ID positivo; la sustitución ocurre en una transacción local. Si se pierde la respuesta, el mismo Idempotency-Key recupera la misma fila del servidor.

Las ediciones se guardan como EDITAR y las eliminaciones como ELIMINAR. La sincronización usa un Mutex: primero sube todas las pendientes y después recorre TODAS las páginas de lotes y labores. Solo al completar la descarga reemplaza filas sincronizadas dentro de una transacción y guarda la fecha de sincronización. Un rechazo de negocio conserva el registro para poder corregirlo. No borra pendientes al fallar la red.

El monitor Android dispara sincronización al recuperar una red validada; también existe el botón manual. La señal de conectividad no garantiza que AgroAPI responda: los errores reales determinan si se conserva la cola.

## Sesión y errores

El almacén cifra la sesión con AES-GCM; la clave AES permanece en Android Keystore. No se guarda el token en texto plano ni se muestra en pantalla o logs. Al reabrir se comprueba expiración, y `/usuarios/me` actualiza privilegios cuando hay red. El 401 elimina la sesión; el 403 conserva la sesión y muestra falta de permiso. El cierre explícito borra el almacén seguro. La base conserva pendientes del mismo usuario; si inicia sesión otro usuario se limpia la caché anterior para no exponer sus datos.

400 se muestra por campo. 404 informa y refresca. 409 muestra el motivo. Falta de red conserva datos; timeout y 5xx se distinguen. CancellationException se relanza.

| Privilegio | TECNICO | JEFE_CAMPO | ADMINISTRADOR |
|---|---|---|---|
| LOTE_LEER | Sí | Sí | Sí |
| LABOR_LEER | Sí | Sí | Sí |
| LABOR_CREAR | Sí | Sí | No |
| LABOR_EDITAR | Propias, antes de 24 h | Todas | No |
| LABOR_ELIMINAR | No | Sí | No |
| CAMPANIA_CERRAR | No | Sí | No |
| USUARIO_GESTIONAR | No | No | Sí |

## Pruebas

Los tests residen en `commonTest` y se ejecutan en el target JVM con JdbcSqliteDriver en memoria y MockEngine de Ktor. Cubren DTO, errores, registro offline, subir antes de bajar, IDs negativos y filtro local. Cada prueba crea y cierra su propia base.

La carpeta `evidencias/` de la entrega incluye capturas del emulador y JSON de respuestas reales. Se verificó que una labor creada en modo avión pasó de ID -1 a ID 22 sin duplicarse. Las fechas e IDs de una nueva ejecución pueden variar.

## Solicitud de cambio

Se prepara `sc-a-perez`: filtro de campañas abiertas combinado con valle y búsqueda, resuelto en SQL local y observable. La rama se entrega como práctica de SC-A; el docente puede asignar una letra distinta.

## Procedencia y trazabilidad

Base: https://github.com/dreyna/pharmaMobil . La entrega registra asistencia de IA. Los commits y ramas creados ahora llevan fechas reales; no se modifica el historial para aparentar desarrollo anterior. La evaluación de la trazabilidad durante la unidad depende de tu historial previo.
