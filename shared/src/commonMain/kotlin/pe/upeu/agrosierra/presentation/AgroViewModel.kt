package pe.upeu.agrosierra.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.AgroRepository
import pe.upeu.agrosierra.domain.usecase.*
data class UiState(val iniciando:Boolean=true,val cargando:Boolean=false,val operacion:Boolean=false,
 val lotes:List<Lote> = emptyList(),val labores:List<Labor> = emptyList(),val seleccionado:Lote?=null,
 val mensaje:String="",val fallo:ErrorApi?=null,val campos:Map<String,String> = emptyMap(),val ultima:String="Nunca",
 val valle:String="",val buscar:String="",val perfil:Boolean=false,val editor:Boolean=false,val editando:Labor?=null)
sealed interface FaseLista {
 data object Cargando:FaseLista
 data object Vacia:FaseLista
 data class Contenido(val lotes:List<Lote>):FaseLista
 data class Error(val causa:ErrorApi):FaseLista
}
val UiState.fase:FaseLista get()=when {
 cargando->FaseLista.Cargando
 lotes.isNotEmpty()->FaseLista.Contenido(lotes)
 fallo!=null->FaseLista.Error(fallo)
 else->FaseLista.Vacia
}
class AgroViewModel(val repo:AgroRepository,val sincronizar:SincronizarUseCase,val guardar:GuardarLaborUseCase,val iniciar:IniciarSesionUseCase,val cerrarSesion:CerrarSesionUseCase):ViewModel() {
 private val _estado=MutableStateFlow(UiState());val estado=_estado.asStateFlow()
 val sesion=repo.sesion
 private var lista:Job?=null;private var detalle:Job?=null
 init {
  observar()
  viewModelScope.launch{repo.ultimaSync.collect{u->_estado.update{it.copy(ultima=u)}}}
  viewModelScope.launch{try{repo.restaurar()}catch(e:FalloApi){error(e)}finally{_estado.update{it.copy(iniciando=false)}};if(sesion.value!=null)sync()}
 }
 private fun observar(){lista?.cancel();lista=viewModelScope.launch{val s=_estado.value;repo.observarLotes(s.valle,s.buscar).collect{l->_estado.update{it.copy(lotes=l,seleccionado=it.seleccionado?.let{anterior->l.find{x->x.id==anterior.id}?:anterior})}}}}
 fun filtros(v:String,b:String){_estado.update{it.copy(valle=v,buscar=b)};observar()}
 fun seleccionar(l:Lote){_estado.update{it.copy(seleccionado=l,mensaje="",perfil=false)};detalle?.cancel();detalle=viewModelScope.launch{repo.observarLabores(l.id).collect{lab->_estado.update{it.copy(labores=lab)}}}}
 fun volver(){_estado.update{it.copy(seleccionado=null,perfil=false,editor=false,mensaje="")}}
 fun perfil(){_estado.update{it.copy(perfil=true,mensaje="")}}
 fun editor(l:Labor?=null){_estado.update{it.copy(editor=true,editando=l,campos=emptyMap(),mensaje="")}}
 fun cancelarEditor(){_estado.update{it.copy(editor=false,campos=emptyMap())}}
 private fun error(e:FalloApi){_estado.update{it.copy(mensaje=e.message.orEmpty(),fallo=e.error,campos=(e.error as? ErrorApi.Validacion)?.campos?:emptyMap())};if(e.error==ErrorApi.NoEncontrado)sync()}
 private fun ejecutar(b:suspend()->Unit)=viewModelScope.launch {
  if(_estado.value.operacion)return@launch
  _estado.update{it.copy(operacion=true,cargando=it.lotes.isEmpty(),mensaje="",fallo=null,campos=emptyMap())}
  try{b()}catch(e:CancellationException){throw e}catch(e:FalloApi){error(e)}catch(e:Exception){_estado.update{it.copy(mensaje="No se pudo completar la operación")}}
  finally{_estado.update{it.copy(operacion=false,cargando=false)}}
 }
 fun login(u:String,p:String)=ejecutar{iniciar(u,p);_estado.update{it.copy(iniciando=false)};val r=sincronizar();_estado.update{it.copy(mensaje="${r.recibidos} registros recibidos")}}
 fun sync()=ejecutar{if(sesion.value!=null){val r=sincronizar();_estado.update{it.copy(mensaje="Enviados: ${r.enviados}. Recibidos: ${r.recibidos}")}}}
 fun salir()=ejecutar{cerrarSesion();_estado.value=UiState(iniciando=false)}
 fun guardarFormulario(tipo:TipoLabor,fecha:String,cantidad:String,observacion:String)=ejecutar {
  val s=_estado.value;val numero=cantidad.toDoubleOrNull()
  if(numero==null)throw FalloApi(ErrorApi.Validacion(mapOf("cantidad" to "Ingrese un número")))
  val labor=s.editando?.copy(tipo=tipo,fecha=fecha,cantidad=numero,observacion=observacion)
   ?:Labor(0,s.seleccionado!!.id,tipo,fecha,sesion.value!!.usuario,numero,observacion,true)
  guardar(labor);_estado.update{it.copy(editor=false,mensaje="Labor guardada. Si falta conexión, queda pendiente")}
 }
 fun eliminar(l:Labor)=ejecutar{repo.eliminar(l);_estado.update{it.copy(mensaje="Eliminación registrada")}}
 fun cerrar()=ejecutar{repo.cerrar(_estado.value.seleccionado!!.id);_estado.update{it.copy(mensaje="Campaña cerrada")}}
}
