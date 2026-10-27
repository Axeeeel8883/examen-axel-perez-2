package pe.upeu.agrosierra
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.upeu.agrosierra.db.AgroDb
import pe.upeu.agrosierra.data.local.BaseLocal
private val drivers=java.util.IdentityHashMap<AgroDb,JdbcSqliteDriver>()
actual fun basePrueba():BaseLocal {
 val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);AgroDb.Schema.create(driver)
 val db=AgroDb(driver);drivers[db]=driver;return BaseLocal(db)
}
actual fun AgroDb.closePrueba(){drivers.remove(this)?.close()}
