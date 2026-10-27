package pe.upeu.agrosierra.presentation
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.usecase.puedeEditarLabor
@Composable fun AgroApp(vm:AgroViewModel) {
 val s by vm.estado.collectAsState();val sesion by vm.sesion.collectAsState()
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF2F6545),secondary=Color(0xFF7D5728),background=Color(0xFFF7F8F2))) {
  Surface(Modifier.fillMaxSize()) {
   Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp)) {
    Text("AgroCampo",style=MaterialTheme.typography.headlineLarge,color=MaterialTheme.colorScheme.primary)
    Text("Cooperativa AgroSierra · labores de campo",style=MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(16.dp))
    if(s.iniciando){CircularProgressIndicator();return@Column}
    if(sesion==null){Login(vm,s);return@Column}
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
     TextButton(onClick=vm::volver){Text("Lotes")}
     TextButton(onClick=vm::perfil){Text("Perfil")}
     Button(onClick={vm.sync()},enabled=!s.operacion){Text("Sincronizar")}
    }
    if(s.operacion)LinearProgressIndicator(Modifier.fillMaxWidth())
    if(s.mensaje.isNotBlank()){Text(s.mensaje,color=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(vertical=8.dp))}
    if(s.perfil){Perfil(vm,sesion!!);return@Column}
    if(s.editor){Formulario(vm,s);return@Column}
    if(s.seleccionado!=null){Detalle(vm,s,sesion!!);return@Column}
    Text("Padrón de lotes",style=MaterialTheme.typography.titleLarge)
    Text("Última sincronización: ${s.ultima}",style=MaterialTheme.typography.bodySmall)
    OutlinedTextField(s.buscar,{vm.filtros(s.valle,it)},label={Text("Buscar nombre o código")},modifier=Modifier.fillMaxWidth())
    OutlinedTextField(s.valle,{vm.filtros(it,s.buscar)},label={Text("Valle (vacío: todos)")},modifier=Modifier.fillMaxWidth())
    Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
     Checkbox(checked=s.soloAbiertas,onCheckedChange=vm::soloAbiertas)
     Text("Solo campañas abiertas")
    }
    if(s.cargando)CircularProgressIndicator()
    if(s.lotes.isEmpty() && !s.cargando){
     Text("No hay lotes guardados con estos filtros",modifier=Modifier.padding(16.dp))
     if(s.fallo!=ErrorApi.NoEncontrado)OutlinedButton(onClick={vm.sync()},enabled=!s.operacion){Text("Reintentar sincronización")}
    }
    LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.weight(1f)) {
     items(s.lotes,key={it.id}){l->Card(onClick={vm.seleccionar(l)},modifier=Modifier.fillMaxWidth()) {
      Column(Modifier.padding(16.dp)) {
       Text("${l.codigo} · ${l.nombre}",style=MaterialTheme.typography.titleMedium)
       Text("${l.valle} · ${l.cultivo} · ${l.hectareas} ha")
       Text(if(l.campaniaCerrada)"Campaña cerrada" else "Campaña abierta")
      }
     }}
    }
   }
  }
 }
}
@Composable private fun Login(vm:AgroViewModel,s:UiState) {
 var usuario by remember{mutableStateOf("")};var password by remember{mutableStateOf("")}
 Text("Iniciar sesión",style=MaterialTheme.typography.titleLarge)
 OutlinedTextField(usuario,{usuario=it},label={Text("Usuario")},modifier=Modifier.fillMaxWidth(),singleLine=true)
 OutlinedTextField(password,{password=it},label={Text("Contraseña")},modifier=Modifier.fillMaxWidth(),singleLine=true,visualTransformation=androidx.compose.ui.text.input.PasswordVisualTransformation())
 Spacer(Modifier.height(12.dp))
 Button(onClick={vm.login(usuario,password)},enabled=!s.operacion && usuario.isNotBlank() && password.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text(if(s.operacion)"Ingresando…" else "Ingresar")}
 if(s.mensaje.isNotBlank())Text(s.mensaje,color=MaterialTheme.colorScheme.error)
}
@Composable private fun Perfil(vm:AgroViewModel,s:Sesion) {
 Text("Perfil",style=MaterialTheme.typography.titleLarge);Text(s.nombre);Text("Usuario: ${s.usuario}");Text("Rol: ${s.rol}")
 Spacer(Modifier.height(12.dp));Text("Privilegios",style=MaterialTheme.typography.titleMedium)
 s.privilegios.sorted().forEach{Text("• $it")}
 Spacer(Modifier.height(20.dp));Button(onClick={vm.salir()}){Text("Cerrar sesión")}
}
@Composable private fun Detalle(vm:AgroViewModel,s:UiState,sesion:Sesion) {
 val l=s.seleccionado!!
 Text(l.nombre,style=MaterialTheme.typography.headlineSmall)
 Text("${l.codigo} · ${l.valle} · ${l.cultivo}")
 Text("${l.hectareas} ha · Dosis máxima/ha: ${l.dosisMaximaPorHectarea}")
 Text(if(l.campaniaCerrada)"Campaña cerrada" else "Campaña abierta")
 if("LABOR_CREAR" in sesion.privilegios && !l.campaniaCerrada)Button(onClick={vm.editor()},enabled=!s.operacion){Text("Registrar labor")}
 if("CAMPANIA_CERRAR" in sesion.privilegios && !l.campaniaCerrada)OutlinedButton(onClick={vm.cerrar()},enabled=!s.operacion){Text("Cerrar campaña")}
 Text("Labores",style=MaterialTheme.typography.titleLarge)
 if(s.labores.isEmpty())Text("Este lote todavía no tiene labores")
 LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) {
  items(s.labores,key={it.id}){labor->Card(Modifier.fillMaxWidth()) {Column(Modifier.padding(12.dp)) {
   Text("${labor.tipo} · ${labor.fecha}",style=MaterialTheme.typography.titleMedium)
   Text("Cantidad: ${labor.cantidad} · ${labor.responsable}");Text(labor.observacion)
   if(labor.pendiente)Text("Pendiente de sincronizar · id ${labor.id}",color=MaterialTheme.colorScheme.secondary)
   Row {
    if(!l.campaniaCerrada && puedeEditarLabor(sesion,labor))TextButton(onClick={vm.editor(labor)},enabled=!s.operacion){Text("Corregir")}
    if("LABOR_ELIMINAR" in sesion.privilegios)TextButton(onClick={vm.eliminar(labor)},enabled=!s.operacion){Text("Eliminar")}
   }
  }}}
 }
}
@Composable private fun Formulario(vm:AgroViewModel,s:UiState) {
 val l=s.editando
 var tipo by remember(l?.id){mutableStateOf(l?.tipo?:TipoLabor.RIEGO)}
 var fecha by remember(l?.id){mutableStateOf(l?.fecha?:kotlinx.datetime.Clock.System.now().toString().take(10))}
 var cantidad by remember(l?.id){mutableStateOf(l?.cantidad?.toString()?:"")}
 var observacion by remember(l?.id){mutableStateOf(l?.observacion?:"")}
 Column(Modifier.verticalScroll(rememberScrollState())) {
  Text(if(l==null)"Registrar labor" else "Corregir labor",style=MaterialTheme.typography.titleLarge)
  Row{TipoLabor.entries.forEach{t->TextButton(onClick={tipo=t}){Text(if(t==tipo)"✓ ${t.name}" else t.name)}}}
  Campo("Fecha (AAAA-MM-DD)",fecha,{fecha=it},s.campos["fecha"])
  Campo("Cantidad aplicada",cantidad,{cantidad=it},s.campos["cantidad"])
  Campo("Observación",observacion,{observacion=it},s.campos["observacion"])
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   Button(onClick={vm.guardarFormulario(tipo,fecha,cantidad,observacion)},enabled=!s.operacion){Text("Guardar")}
   TextButton(onClick=vm::cancelarEditor){Text("Cancelar")}
  }
 }
}
@Composable private fun Campo(etiqueta:String,valor:String,cambiar:(String)->Unit,error:String?) {
 OutlinedTextField(valor,cambiar,label={Text(etiqueta)},isError=error!=null,supportingText={if(error!=null)Text(error)},modifier=Modifier.fillMaxWidth())
}
