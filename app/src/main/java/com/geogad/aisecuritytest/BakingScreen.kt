package com.geogad.aisecuritytest

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel


val images = arrayOf(
    R.drawable.baked_goods_1,
    R.drawable.baked_goods_2,
    R.drawable.baked_goods_3
)
val imageDescriptions = arrayOf(
    R.string.image1_description,
    R.string.image2_description,
    R.string.image3_description
)

@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val selectedImage = remember { mutableIntStateOf(0) }
    val placeholderPrompt = stringResource(R.string.prompt_test)

   // val placeholderPrompt = stringResource(R.string.prompt_placeholder)
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val TAG = "BakingScreen"

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.baking_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(images) { index, image ->
                var imageModifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .requiredSize(200.dp)
                    .clickable {
                        selectedImage.intValue = index
                    }
                if (index == selectedImage.intValue) {
                    imageModifier =
                        imageModifier.border(BorderStroke(4.dp, MaterialTheme.colorScheme.primary))
                }
                Image(
                    painter = painterResource(image),
                    contentDescription = stringResource(imageDescriptions[index]),
                    modifier = imageModifier
                )
            }
        }

        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            TextField(
                value = prompt,
                label = { Text(stringResource(R.string.label_prompt)) },
                onValueChange = { prompt = it },
                modifier = Modifier
                    .weight(0.8f)
                    .padding(end = 16.dp)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = {
                    val bitmap = BitmapFactory.decodeResource(
                        context.resources,
                        images[selectedImage.intValue]
                    )
                  //  bakingViewModel.sendPrompt(bitmap, prompt)
                    bakingViewModel.sendPrompt(prompt)
                },
                enabled = prompt.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(R.string.action_go))
            }
        }

        if (uiState is UiState.Loading) {
            Log.w("Bakingscreen", "Progressing...")
           // CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            var textColor = MaterialTheme.colorScheme.onSurface
            if (uiState is UiState.Error) {
                textColor = MaterialTheme.colorScheme.error
                result = (uiState as UiState.Error).errorMessage
            } else if (uiState is UiState.Success) {
                textColor = MaterialTheme.colorScheme.onSurface
                result = (uiState as UiState.Success).outputText
            }
            Log.w("BakingScreen", "Result is $result")
            WebView("This code")
            if (result.contains("'''")) {
                Log.w("BakingScreen", "Time To work on $result")
                val qqq = Regex("'''")
                val matches : Sequence<MatchResult> = qqq.findAll(result)
               // if (matches.first())
                matches.forEachIndexed { index, matchResult ->
                    Log.w("BakingScreen", "$index) matchResult = $matchResult")
                }
                /*
              //  var codeLast = result.indexOfLast (qqq)
                Log.w("BakingScreen", "codeStart, codeLast = $codeStart, $codeLast")
                if ((codeStart > 10) && (codeLast > 10)) {
                    var code2Run = result.substring(codeStart, codeLast)
                    Log.w("BakingScreen", " code2Run =  $code2Run")
                    val myWebView = WebView(context)

                  //  setContentView(myWebView)
                }
                */
                Log.w("BakingScreen", "Time To work on $result")
            }

            val scrollState = rememberScrollState()
            Text(
                text = result,
                textAlign = TextAlign.Start,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            )
        }
    }
}

@Composable
fun WebView(code: String){

    // Declare a string that contains a url
    val mUrl = "https://www.google.com"

    // Adding a WebView inside AndroidView
    // with layout as full screen
    AndroidView(factory = {
        WebView(it).apply {
            true.also { this.settings.javaScriptEnabled = it }
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.webChromeClient = CustomWebChromeClient()
            this.webViewClient = CustomWebViewClient()

//            val intent= Intent(Intent.ACTION_VIEW).apply {
//                type="text/plain"
//                putExtra(Intent.EXTRA_EMAIL, arrayListOf("youremailid@gmail.com"))
//                putExtra(Intent.EXTRA_SUBJECT, "This is the subject of the mail")
//                putExtra(Intent.EXTRA_TEXT, "This is the text part of the mail")
//            }
//            if(intent.resolveActivity(packageManager)!=null){
//                startActivity(intent)
//            }
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.setType("vnd.android.cursor.dir/call")
//            startActivity(intent)
            val intent= Intent(Intent.ACTION_VIEW).apply {
                type="vnd.android.cursor.dir/call"
                putExtra(Intent.EXTRA_EMAIL, arrayListOf("youremailid@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "This is the subject of the mail")
                putExtra(Intent.EXTRA_TEXT, "This is the text part of the mail")
            }
            if(intent.resolveActivity(context.packageManager)!=null){
                startActivity(context, intent, null)
            }
            Log.w("In IntentSender", "After activity")
        }

    }, update = {
        it.loadUrl(mUrl)
    })
}
class CustomWebViewClient: WebViewClient(){
    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if(url != null && url.startsWith("https://google.com")){
            return true
        }
        return false
    }
}

class CustomWebChromeClient : WebChromeClient() {
    override fun onCloseWindow(window: WebView?) {
        Log.w("In ChromeClient", "Closing...")
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        Log.w("In ChromeClient", "OnConsoleMessage = $consoleMessage")
        return true
    }
}