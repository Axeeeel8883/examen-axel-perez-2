package pe.upeu.agrosierra
import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.errors.IOException
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.data.repository.*
import pe.upeu.agrosierra.domain.model.*
expect fun basePrueba():BaseLocal
class SeguroPrueba:AlmacenSeguro {
 var s:Sesion?=Sesion("token","tecnico1","TECNICO",setOf("LOTE_LEER","LABOR_LEER","LABOR_CREAR","LABOR_EDITAR"),Long.MAX_VALUE)
 override fun leer()=s
 override fun guardar(s:Sesion){this.s=s}
 override fun borrar(){s=null}
}
class RepositorioTest {
 private fun labor()=Labor(0,1,TipoLabor.RIEGO,"2026-10-27","tecnico1",1.0,"Prueba",true)
 private val reloj=object:Reloj{override fun milis()=1000L;override fun instante()="2026-10-27T00:00:00Z"}
 @Test fun conversionDto(){val d=LoteDto(1,"L1","Lote",2.0,"Papa","Mantaro",false,3.0);assertEquals(3.0,d.dominio().dosisMaximaPorHectarea)}
 @Test fun traduccionError(){assertEquals(ErrorApi.Validacion(mapOf("cantidad" to "Máximo")),traducir(400,ErrorDto(validationErrors=mapOf("cantidad" to "Máximo"))));assertEquals(ErrorApi.SinPrivilegio,traducir(403,ErrorDto()))}
 @Test fun registrarSinRed()=runTest {
  val base=basePrueba();val seguro=SeguroPrueba();val c=crearCliente(MockEngine{throw IOException("Sin red")},ApiConfig("https://agro.test/api/v1/"),seguro)
  try{val r=AgroRepositorioOffline(AgroApi(c),base,seguro,reloj);r.restaurar();r.guardar(labor());val l=r.observarLabores(1).first().single();assertTrue(l.id<0);assertTrue(l.pendiente)}finally{c.close();base.db.closePrueba()}
 }
 @Test fun subirAntesDeBajar()=runTest {
  val base=basePrueba();val seguro=SeguroPrueba();val rutas=mutableListOf<String>()
  base.guardar(labor().copy(id=-1),"CREAR","estable")
  val c=crearCliente(MockEngine{req->rutas+="${req.method.value} ${req.url.encodedPath}"
   val body=when {
    req.url.encodedPath.endsWith("/usuarios/me")->"""{"usuario":"tecnico1","rol":"TECNICO","privilegios":["LABOR_CREAR"]}"""
    req.method==HttpMethod.Post->"""{"id":10,"loteId":1,"tipo":"RIEGO","fecha":"2026-10-27","responsable":"tecnico1","cantidad":1.0,"observacion":"Prueba"}"""
    req.url.encodedPath.endsWith("/lotes")->"""{"contenido":[{"id":1,"codigo":"L1","nombre":"Lote","hectareas":2.0,"cultivo":"Papa","valle":"Mantaro","campaniaCerrada":false,"dosisMaximaPorHectarea":3.0}],"pagina":0,"tamanio":100,"totalElementos":1,"totalPaginas":1,"ultima":true}"""
    else->"""{"contenido":[{"id":10,"loteId":1,"tipo":"RIEGO","fecha":"2026-10-27","responsable":"tecnico1","cantidad":1.0,"observacion":"Prueba"}],"pagina":0,"tamanio":100,"totalElementos":1,"totalPaginas":1,"ultima":true}"""
   };respond(body,HttpStatusCode.OK,headersOf(HttpHeaders.ContentType,"application/json"))
  },ApiConfig("https://agro.test/api/v1/"),seguro)
  try{val r=AgroRepositorioOffline(AgroApi(c),base,seguro,reloj);r.restaurar();r.sincronizar();assertTrue(rutas.indexOf("POST /api/v1/labores")<rutas.indexOf("GET /api/v1/lotes"));assertEquals(10L,r.observarLabores(1).first().single().id);assertTrue(base.pendientes().isEmpty())}finally{c.close();base.db.closePrueba()}
 }
 @Test fun idsNegativosDistintos(){val b=basePrueba();try{b.guardar(labor().copy(id=b.idLocal()));b.guardar(labor().copy(id=b.idLocal()));assertEquals(setOf(-1L,-2L),b.pendientes().map{it.labor.id}.toSet())}finally{b.db.closePrueba()}}
 @Test fun filtroLocalFuncionaSinRed()=runTest{val b=basePrueba();try{b.guardar(Lote(1,"A","Papa",1.0,"Papa","Mantaro",false,2.0));b.guardar(Lote(2,"B","Maíz",1.0,"Maíz","Colca",true,2.0));assertEquals(1,b.lotes("Mantaro","A",true).first().size)}finally{b.db.closePrueba()}}
}
// El driver pertenece al fixture y se cierra al terminar cada prueba.
expect fun pe.upeu.agrosierra.db.AgroDb.closePrueba()

