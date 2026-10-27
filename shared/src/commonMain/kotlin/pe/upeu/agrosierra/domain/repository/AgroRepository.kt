package pe.upeu.agrosierra.domain.repository
import kotlinx.coroutines.flow.*
import pe.upeu.agrosierra.domain.model.*
interface AgroRepository {
 val sesion:StateFlow<Sesion?>
 val ultimaSync:Flow<String>
 fun observarLotes(valle:String="",buscar:String="",soloAbiertas:Boolean=false):Flow<List<Lote>>
 fun observarLabores(loteId:Long):Flow<List<Labor>>
 fun pendientesPorLote(loteId:Long):Flow<Long>
 suspend fun login(usuario:String,password:String)
 suspend fun restaurar()
 suspend fun salir()
 suspend fun guardar(labor:Labor)
 suspend fun eliminar(labor:Labor)
 suspend fun cerrar(loteId:Long)
 suspend fun sincronizar():ResumenSync
}
