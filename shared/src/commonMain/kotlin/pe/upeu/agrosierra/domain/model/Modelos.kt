package pe.upeu.agrosierra.domain.model
data class Lote(val id:Long,val codigo:String,val nombre:String,val hectareas:Double,val cultivo:String,
 val valle:String,val campaniaCerrada:Boolean,val dosisMaximaPorHectarea:Double)
enum class TipoLabor { RIEGO, FUMIGACION, COSECHA }
data class Labor(val id:Long,val loteId:Long,val tipo:TipoLabor,val fecha:String,val responsable:String,
 val cantidad:Double,val observacion:String,val pendiente:Boolean,val registradoEn:String="")
data class Sesion(val token:String,val usuario:String,val rol:String,val privilegios:Set<String>,
 val expiraEn:Long=0,val nombre:String=usuario,val edicionGlobal:Boolean=false)
data class ResumenSync(val enviados:Int,val recibidos:Int)
sealed interface ErrorApi {
 data class Validacion(val campos:Map<String,String>):ErrorApi
 data object CredencialesIncorrectas:ErrorApi
 data object NoAutorizado:ErrorApi
 data object SinPrivilegio:ErrorApi
 data object NoEncontrado:ErrorApi
 data class Conflicto(val motivo:String):ErrorApi
 data object Servidor:ErrorApi
 data object SinConexion:ErrorApi
 data object TiempoAgotado:ErrorApi
 data object Formato:ErrorApi
}
class FalloApi(val error:ErrorApi):Exception(mensaje(error))
fun mensaje(e:ErrorApi):String=when(e) {
 is ErrorApi.Validacion->"Revise los campos indicados"
 ErrorApi.CredencialesIncorrectas->"Usuario o contraseña incorrectos"
 ErrorApi.NoAutorizado->"Inicie sesión nuevamente"
 ErrorApi.SinPrivilegio->"No tiene permiso para esta operación"
 ErrorApi.NoEncontrado->"El recurso ya no existe"
 is ErrorApi.Conflicto->e.motivo
 ErrorApi.Servidor->"El servidor no responde correctamente. Puede reintentar"
 ErrorApi.SinConexion->"Sin conexión. Sus datos locales se conservan"
 ErrorApi.TiempoAgotado->"Se agotó el tiempo de espera"
 ErrorApi.Formato->"La respuesta no tiene el formato esperado"
}
