package com.sankalp.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.util.UUID

data class MediaItem(val id:String=UUID.randomUUID().toString(), val name:String, val uri:String, val type:String)

class MainActivity : ComponentActivity() {
    private var lastBack = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SankalpApp() }
    }
    fun exitOnDoubleBack() {
        val now=System.currentTimeMillis()
        if(now-lastBack < 1800) finish() else { lastBack=now; android.widget.Toast.makeText(this,"Press back again to exit", android.widget.Toast.LENGTH_SHORT).show() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SankalpApp() {
    val ctx=LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var count by rememberSaveable { mutableIntStateOf(0) }
    var target by rememberSaveable { mutableIntStateOf(108) }
    var sankalp by rememberSaveable { mutableStateOf("") }
    var media by remember { mutableStateOf(listOf<MediaItem>()) }
    var webUrl by rememberSaveable { mutableStateOf("https://www.google.com") }
    val picker = rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        media = media + uris.map { MediaItem(name=it.lastPathSegment ?: "Media", uri=it.toString(), type="file") }
    }

    BackHandler {
        if(tab != 0) tab=0 else (ctx as? MainActivity)?.exitOnDoubleBack()
    }

    Scaffold(
        topBar={ TopAppBar(title={ Text("Sankalp") }) },
        bottomBar={
            NavigationBar {
                listOf("Home","Mantra","Audio","Video","Images","Browser","Settings").forEachIndexed { i,n ->
                    NavigationBarItem(selected=tab==i, onClick={tab=i}, icon={}, label={Text(n)})
                }
            }
        }
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp)) {
            when(tab) {
                0 -> Home(sankalp, {sankalp=it}, count, target, {count=0}, { if(count<target) count++ })
                1 -> Mantra(count,target,{count=0},{count++},{target=it})
                2 -> MediaScreen("Audio", media.filter{it.type=="audio"||it.type=="file"}, {picker.launch(arrayOf("audio/*"))}, {x->media=media.filterNot{it.id==x.id}}, ctx)
                3 -> MediaScreen("Video", media.filter{it.type=="video"||it.type=="file"}, {picker.launch(arrayOf("video/*"))}, {x->media=media.filterNot{it.id==x.id}}, ctx)
                4 -> MediaScreen("Images", media.filter{it.type=="image"||it.type=="file"}, {picker.launch(arrayOf("image/*"))}, {x->media=media.filterNot{it.id==x.id}}, ctx)
                5 -> Browser(webUrl,{webUrl=it})
                6 -> SettingsScreen(media.size,{picker.launch(arrayOf("*/*"))})
            }
        }
    }
}

@Composable fun Home(s:String,set:(String)->Unit,c:Int,t:Int,reset:()->Unit,inc:()->Unit){
    Text("Daily Sankalp",style=MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(12.dp)); OutlinedTextField(s,set,label={Text("Today's Sankalp")},modifier=Modifier.fillMaxWidth())
    Spacer(Modifier.height(24.dp)); Text("Mantra progress: $c / $t")
    LinearProgressIndicator({if(t==0)0f else c.toFloat()/t},Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp)); Button(onClick=inc,Modifier.fillMaxWidth()){Text("Tap Mantra")}
    OutlinedButton(onClick=reset,Modifier.fillMaxWidth()){Text("Reset")}
}
@Composable fun Mantra(c:Int,t:Int,reset:()->Unit,inc:()->Unit,setTarget:(Int)->Unit){
    Text("Mantra Counter",style=MaterialTheme.typography.headlineMedium)
    Text("$c / $t",style=MaterialTheme.typography.displaySmall)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(108,1001).forEach{Button(onClick={setTarget(it);reset()}){Text("$it")}}}
    OutlinedButton(onClick={reset}){Text("Reset")}; Spacer(Modifier.height(12.dp))
    Button(onClick=inc,modifier=Modifier.fillMaxWidth().height(100.dp)){Text("TAP",style=MaterialTheme.typography.headlineLarge)}
}
@Composable fun MediaScreen(title:String,list:List<MediaItem>,add:()->Unit,del:(MediaItem)->Unit,ctx:android.content.Context){
    Text(title,style=MaterialTheme.typography.headlineMedium); Button(onClick=add){Text("Add $title")}
    LazyColumn { items(list){m->Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.SpaceBetween){
        Text(m.name,Modifier.weight(1f))
        TextButton(onClick={del(m)}){Text("Delete")}
    }}}
}
@Composable fun Browser(url:String,setUrl:(String)->Unit){
    Text("Browser",style=MaterialTheme.typography.headlineMedium)
    var text by remember{mutableStateOf(url)}
    Row{OutlinedTextField(text,{text=it},Modifier.weight(1f),singleLine=true);Button(onClick={setUrl(text)}){Text("Go")}}
    Spacer(Modifier.height(8.dp))
    AndroidView(factory={c->WebView(c).apply{webViewClient=WebViewClient();settings.javaScriptEnabled=true;loadUrl(url)}},update={it.loadUrl(url)},modifier=Modifier.fillMaxSize())
}
@Composable fun SettingsScreen(n:Int,add:()->Unit){
    Text("Settings",style=MaterialTheme.typography.headlineMedium)
    Text("Local media items: $n")
    Spacer(Modifier.height(12.dp)); Button(onClick=add){Text("Import content")}
    Text("Future-ready: more mantras, meditation, Tai-Chi music, Hanuman Chalisa, Om chanting and voice controls.")
}
