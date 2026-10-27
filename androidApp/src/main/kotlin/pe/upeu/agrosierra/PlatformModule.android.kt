package pe.upeu.agrosierra
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.engine.okhttp.OkHttp
import pe.upeu.agrosierra.db.AgroDb
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.data.remote.*
val platformModule=module {
 single<AlmacenSeguro>{AlmacenSeguroAndroid(androidContext())}
 single{BaseLocal(AgroDb(AndroidSqliteDriver(AgroDb.Schema,androidContext(),"agro.db")))}
 single{crearCliente(OkHttp.create(),ApiConfig(),get(),BuildConfig.DEBUG)}
}
