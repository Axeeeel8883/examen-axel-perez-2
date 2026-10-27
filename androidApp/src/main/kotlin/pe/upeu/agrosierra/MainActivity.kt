package pe.upeu.agrosierra
import android.os.Bundle
import android.net.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import org.koin.core.context.GlobalContext
import pe.upeu.agrosierra.presentation.*
class MainActivity:ComponentActivity(){
 private lateinit var vm:AgroViewModel
 private val monitor by lazy{getSystemService(ConnectivityManager::class.java)}
 private val callback=object:ConnectivityManager.NetworkCallback(){
  override fun onCapabilitiesChanged(n:Network,c:NetworkCapabilities){if(c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) && ::vm.isInitialized)runOnUiThread{vm.sync()}}
 }
 override fun onCreate(b:Bundle?){super.onCreate(b)
  vm=ViewModelProvider(this,object:ViewModelProvider.Factory{override fun <T:ViewModel> create(modelClass:Class<T>):T {@Suppress("UNCHECKED_CAST") return GlobalContext.get().get<AgroViewModel>() as T}})[AgroViewModel::class.java]
  monitor.registerDefaultNetworkCallback(callback)
  setContent{AgroApp(vm)}
 }
 override fun onDestroy(){monitor.unregisterNetworkCallback(callback);super.onDestroy()}
}
