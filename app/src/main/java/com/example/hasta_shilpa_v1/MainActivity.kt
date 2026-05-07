package com.example.hasta_shilpa_v1

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hasta_shilpa_v1.ui.theme.Hasta_shilpa_v1Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ForestGreen = Color(0xFF1B4332)
private val LeafGreen = Color(0xFF2D6A4F)
private val WarmCream = Color(0xFFF8F4E3)
private val Amber = Color(0xFFF4A261)
private val OffWhite = Color(0xFFFFFCF2)
private val Ink = Color(0xFF24352E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hasta_shilpa_v1Theme {
                HastaShilpaApp()
            }
        }
    }
}

@Composable
fun HastaShilpaApp() {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Feed) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var marketMode by rememberSaveable { mutableStateOf(MarketMode.Price) }
    val generatedPrice = rememberSaveable { mutableStateOf("1,620") }

    Scaffold(
        containerColor = WarmCream,
        bottomBar = {
            NavigationBar(containerColor = OffWhite, tonalElevation = 8.dp) {
                bottomDestinations.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item.destination && selectedProduct == null,
                        onClick = {
                            selectedProduct = null
                            destination = item.destination
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                selectedProduct != null -> BlueprintScreen(
                    product = selectedProduct!!,
                    onGenerateSimilar = {
                        selectedProduct = null
                        destination = AppDestination.Generate
                    }
                )

                destination == AppDestination.Home -> HomeScreen()
                destination == AppDestination.Feed -> FeedScreen(onOpenBlueprint = { selectedProduct = it })
                destination == AppDestination.Generate -> GeneratorScreen()
                destination == AppDestination.Tracker -> TrackerScreen()
                destination == AppDestination.Market -> MarketScreen(
                    mode = marketMode,
                    onModeChange = { marketMode = it },
                    price = generatedPrice.value,
                    onPriceCalculated = { generatedPrice.value = it }
                )
            }
        }
    }
}

private enum class AppDestination { Home, Feed, Generate, Tracker, Market }
private enum class MarketMode { Price, Listing }

private data class NavItem(val label: String, val destination: AppDestination, val icon: ImageVector)
private data class Product(val name: String, val category: String, val style: Int)
private data class Batch(val name: String, val date: String, val poles: String, val strips: String)

private val bottomDestinations = listOf(
    NavItem("Feed", AppDestination.Feed, Icons.Default.Home),
    NavItem("Generate", AppDestination.Generate, Icons.Default.Add),
    NavItem("Tracker", AppDestination.Tracker, Icons.Default.AccountBox),
    NavItem("Market", AppDestination.Market, Icons.Default.Favorite),
)

private val products = listOf(
    Product("Foldable Bamboo Laptop Stand", "Laptop Stand", 0),
    Product("Woven Amber Pendant Lamp", "Lamp Shade", 1),
    Product("Cane Storage Basket Set", "Basket", 2),
    Product("Curved Bamboo Wall Shelf", "Wall Decor", 3),
    Product("Stackable Cane Fruit Tray", "Kitchen Ware", 2),
    Product("Bamboo Phone Speaker Dock", "Phone Stand", 0),
    Product("Palm Leaf Wall Mirror", "Wall Decor", 3),
    Product("Cane Tea Light Lantern", "Lantern", 1),
    Product("Folded Bamboo Side Table", "Table", 0),
    Product("Woven Utility Tote", "Storage Bag", 2),
)

@Composable
private fun HomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestGreen)
            .bambooPattern()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .background(OffWhite),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(96.dp)) {
                    drawCircle(Amber.copy(alpha = 0.32f), radius = size.minDimension * 0.42f)
                    drawLine(LeafGreen, Offset(size.width * 0.3f, size.height * 0.82f), Offset(size.width * 0.7f, size.height * 0.18f), 18f, StrokeCap.Round)
                    drawLine(ForestGreen, Offset(size.width * 0.52f, size.height * 0.82f), Offset(size.width * 0.82f, size.height * 0.3f), 12f, StrokeCap.Round)
                    drawCircle(ForestGreen, radius = 12f, center = Offset(size.width * 0.34f, size.height * 0.3f))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Hasta-Shilpa", color = OffWhite, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text(
                "Design Bridge for Artisans",
                color = WarmCream,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun FeedScreen(onOpenBlueprint: (Product) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                placeholder = { Text("Search bamboo designs") },
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(products) { product ->
            ProductCard(product = product, onClick = { onOpenBlueprint(product) })
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = OffWhite),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        BambooProductArt(style = product.style, modifier = Modifier.fillMaxWidth().height(220.dp))
        Column(Modifier.padding(18.dp)) {
            Text(product.name, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Chip(product.category)
        }
    }
}

@Composable
private fun BlueprintScreen(product: Product, onGenerateSimilar: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scale by rememberSaveable { mutableStateOf(1f) }
    var offsetX by rememberSaveable { mutableStateOf(0f) }
    var offsetY by rememberSaveable { mutableStateOf(0f) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offsetX += panChange.x
        offsetY += panChange.y
    }
    val saveBlueprint = {
        scope.launch {
            val saved = saveBlueprintToGallery(context, product)
            val message = if (saved) "Blueprint saved to gallery" else "Could not save blueprint"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            saveBlueprint()
        } else {
            Toast.makeText(context, "Storage permission is needed to save blueprints", Toast.LENGTH_SHORT).show()
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .transformable(transformableState)
        ) {
            BambooProductArt(style = product.style, modifier = Modifier.fillMaxSize())
            BlueprintAnnotations()
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(18.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.42f))
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("${scale.toInt()}x", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = OffWhite,
            tonalElevation = 8.dp
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(product.name, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        val needsLegacyPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        if (needsLegacyPermission) {
                            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            saveBlueprint()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("Download Blueprint", fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = onGenerateSimilar,
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) { Text("Generate Similar", color = ForestGreen, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun BlueprintAnnotations() {
    Canvas(Modifier.fillMaxSize().padding(30.dp)) {
        val white = Color.White.copy(alpha = 0.92f)
        drawLine(white, Offset(size.width * 0.18f, size.height * 0.28f), Offset(size.width * 0.82f, size.height * 0.28f), 3f)
        drawLine(white, Offset(size.width * 0.78f, size.height * 0.24f), Offset(size.width * 0.78f, size.height * 0.62f), 3f)
        drawLine(white, Offset(size.width * 0.22f, size.height * 0.65f), Offset(size.width * 0.55f, size.height * 0.78f), 3f)
    }
    Text("30 cm", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 96.dp, top = 110.dp))
    Text("15 cm", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 304.dp, top = 260.dp))
    Text("8 cm", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 130.dp, top = 520.dp))
}

@Composable
private fun GeneratorScreen() {
    var prompt by rememberSaveable { mutableStateOf("") }
    var generated by rememberSaveable { mutableStateOf(false) }
    var generating by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(generating) {
        if (generating) {
            delay(1200)
            generated = true
            generating = false
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenTitle("AI Design Generator")
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = { Text("Describe a product... e.g. modern bamboo lamp shade") },
            minLines = 4,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                generated = false
                generating = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text(if (generating) "Generating..." else "Generate", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        ResultCard(generated = generated, loading = generating)
    }
}

@Composable
private fun ResultCard(generated: Boolean, loading: Boolean) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = OffWhite), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (generated) {
                BambooProductArt(style = 1, modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(14.dp)))
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Save to Blueprints", color = ForestGreen) }
            } else if (loading) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFD4C79D), Color(0xFFF5E9CC), Color(0xFFD4C79D)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Making design...", color = Ink.copy(alpha = 0.72f), fontWeight = FontWeight.Bold)
                }
                Text("Preparing preview", color = Ink.copy(alpha = 0.72f))
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE5DFC8), Color(0xFFF5E9CC), Color(0xFFE5DFC8)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Generated image appears here", color = Ink.copy(alpha = 0.62f), fontWeight = FontWeight.SemiBold)
                }
                Text("Preview area", color = Ink.copy(alpha = 0.58f))
            }
        }
    }
}

@Composable
private fun TrackerScreen() {
    var name by rememberSaveable { mutableStateOf("") }
    var poles by rememberSaveable { mutableStateOf("") }
    var strips by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("07 May 2026") }
    val batches = remember {
        mutableStateListOf(
            Batch("Lamp Shade Batch", "06 May 2026", "18", "64"),
            Batch("Laptop Stand Batch", "03 May 2026", "10", "28")
        )
    }
    Column(Modifier.fillMaxSize().background(WarmCream).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScreenTitle("Material Tracker")
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = OffWhite)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InputField("Batch Name", name) { name = it }
                InputField("Bamboo Poles Used", poles, KeyboardType.Number) { poles = it }
                InputField("Cane Strips Used", strips, KeyboardType.Number) { strips = it }
                InputField("Date", date) { date = it }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            batches.add(0, Batch(name, date, poles.ifBlank { "0" }, strips.ifBlank { "0" }))
                            name = ""; poles = ""; strips = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Log Batch", fontWeight = FontWeight.Bold) }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(batches) { batch ->
                BatchCard(batch = batch, onDelete = { batches.remove(batch) })
            }
        }
    }
}

@Composable
private fun BatchCard(batch: Batch, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = OffWhite), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(batch.name, color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(batch.date, color = Ink.copy(alpha = 0.68f))
                Text("${batch.poles} poles  |  ${batch.strips} cane strips", color = ForestGreen, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ForestGreen) }
        }
    }
}

@Composable
private fun MarketScreen(
    mode: MarketMode,
    onModeChange: (MarketMode) -> Unit,
    price: String,
    onPriceCalculated: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().background(WarmCream).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ModeButton("Price", mode == MarketMode.Price, Modifier.weight(1f)) { onModeChange(MarketMode.Price) }
            ModeButton("Listing", mode == MarketMode.Listing, Modifier.weight(1f)) { onModeChange(MarketMode.Listing) }
        }
        if (mode == MarketMode.Price) {
            PriceSuggesterScreen(onPriceCalculated)
        } else {
            MarketplaceScreen(price)
        }
    }
}

@Composable
private fun PriceSuggesterScreen(onPriceCalculated: (String) -> Unit) {
    var material by rememberSaveable { mutableStateOf("900") }
    var hours by rememberSaveable { mutableStateOf("6") }
    var overhead by rememberSaveable { mutableStateOf("20") }
    var result by rememberSaveable { mutableStateOf("1,620") }
    fun calculate() {
        val cost = material.toIntOrNull() ?: 0
        val labour = (hours.toIntOrNull() ?: 0) * 90
        val final = ((cost + labour) * (1 + ((overhead.toFloatOrNull() ?: 20f) / 100f))).toInt()
        result = "%,d".format(final)
        onPriceCalculated(result)
    }
    ScreenTitle("Price Suggester")
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = OffWhite), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InputField("Material Cost (\u20B9)", material, KeyboardType.Number) { material = it }
            InputField("Hours Worked", hours, KeyboardType.Number) { hours = it }
            InputField("Overhead %", overhead, KeyboardType.Number) { overhead = it }
            Button(onClick = ::calculate, colors = ButtonDefaults.buttonColors(containerColor = Amber), modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Text("Calculate", color = Color(0xFF3A2716), fontWeight = FontWeight.Bold)
            }
            Text("\u20B9$result", color = ForestGreen, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Text("Cost + Labour + Overhead", color = Ink, fontWeight = FontWeight.SemiBold)
            Text("Material \u20B9$material  |  Labour \u20B9${(hours.toIntOrNull() ?: 0) * 90}", color = Ink.copy(alpha = 0.72f))
            Text("Overhead $overhead%  =  Final \u20B9$result", color = Ink.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun MarketplaceScreen(price: String) {
    var listed by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("Woven Amber Pendant Lamp") }
    var listingPrice by rememberSaveable(price) { mutableStateOf("\u20B9$price") }
    var description by rememberSaveable { mutableStateOf("Hand woven bamboo lamp shade with warm amber finish.") }
    ScreenTitle("Simulated Marketplace")
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = OffWhite), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InputField("Product Name", name) { name = it }
            InputField("Price", listingPrice, KeyboardType.Number) { listingPrice = it }
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, ForestGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .background(WarmCream),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = "Add photo", tint = ForestGreen, modifier = Modifier.size(42.dp))
                    Text("Add Photo", color = ForestGreen, fontWeight = FontWeight.Bold)
                }
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { listed = true }, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen), modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Text("List My Product", fontWeight = FontWeight.Bold)
            }
        }
    }
    AnimatedVisibility(listed) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = OffWhite), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(58.dp).clip(CircleShape).background(ForestGreen), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(34.dp))
                }
                Text("Product Listed Successfully!", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                OutlinedButton(onClick = {}) { Text("View Listing", color = ForestGreen) }
            }
        }
    }
}

@Composable
private fun BambooProductArt(style: Int, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFEFE5C9), Color(0xFFCDAF77))))
    ) {
        val w = size.width
        val h = size.height
        drawRoundRect(Color(0xFFE8D9B6), cornerRadius = CornerRadius(34f, 34f))
        repeat(7) { i ->
            val x = (i + 1) * w / 8f
            drawLine(Color(0xFF8D6E38).copy(alpha = 0.3f), Offset(x, h * 0.08f), Offset(x - 30f, h * 0.92f), 8f, StrokeCap.Round)
        }
        when (style % 4) {
            0 -> {
                drawRoundRect(ForestGreen.copy(alpha = 0.85f), Offset(w * 0.18f, h * 0.58f), androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.08f), CornerRadius(18f))
                drawRoundRect(Color(0xFFB98743), Offset(w * 0.28f, h * 0.34f), androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.2f), CornerRadius(24f))
                drawLine(Color(0xFF6F4F22), Offset(w * 0.34f, h * 0.66f), Offset(w * 0.22f, h * 0.88f), 16f, StrokeCap.Round)
                drawLine(Color(0xFF6F4F22), Offset(w * 0.66f, h * 0.66f), Offset(w * 0.78f, h * 0.88f), 16f, StrokeCap.Round)
            }
            1 -> {
                drawCircle(Amber.copy(alpha = 0.55f), radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.48f))
                drawLine(Color(0xFF4F3420), Offset(w * 0.5f, h * 0.08f), Offset(w * 0.5f, h * 0.35f), 10f, StrokeCap.Round)
                drawRoundRect(Color(0xFFB77C35), Offset(w * 0.24f, h * 0.32f), androidx.compose.ui.geometry.Size(w * 0.52f, h * 0.28f), CornerRadius(52f))
            }
            2 -> {
                drawRoundRect(Color(0xFFB98743), Offset(w * 0.2f, h * 0.36f), androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.34f), CornerRadius(42f))
                repeat(5) { i -> drawLine(ForestGreen.copy(alpha = 0.35f), Offset(w * (0.25f + i * 0.1f), h * 0.38f), Offset(w * (0.3f + i * 0.08f), h * 0.68f), 7f) }
            }
            else -> {
                drawRoundRect(Color(0xFFB98743), Offset(w * 0.16f, h * 0.28f), androidx.compose.ui.geometry.Size(w * 0.68f, h * 0.12f), CornerRadius(24f))
                drawRoundRect(Color(0xFF6F4F22), Offset(w * 0.22f, h * 0.45f), androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.1f), CornerRadius(24f))
                drawRoundRect(Color(0xFFB98743), Offset(w * 0.28f, h * 0.62f), androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.1f), CornerRadius(24f))
            }
        }
    }
}

private fun saveBlueprintToGallery(context: Context, product: Product): Boolean {
    return try {
        val bitmap = createBlueprintBitmap(product)
        val fileName = "hasta_shilpa_${product.name.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Hasta-Shilpa")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        } ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        true
    } catch (_: Exception) {
        false
    }
}

private fun createBlueprintBitmap(product: Product): Bitmap {
    val width = 1080
    val height = 1440
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = AndroidColor.rgb(232, 217, 182)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.strokeWidth = 12f
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = AndroidColor.argb(82, 141, 110, 56)
    repeat(12) { index ->
        val x = (index + 1) * width / 13f
        canvas.drawLine(x, 120f, x - 70f, height - 180f, paint)
    }

    drawProductShape(canvas, paint, product.style, width.toFloat(), height.toFloat())

    paint.color = AndroidColor.WHITE
    paint.strokeWidth = 5f
    canvas.drawLine(width * 0.18f, height * 0.24f, width * 0.82f, height * 0.24f, paint)
    canvas.drawLine(width * 0.78f, height * 0.22f, width * 0.78f, height * 0.58f, paint)
    canvas.drawLine(width * 0.22f, height * 0.62f, width * 0.55f, height * 0.74f, paint)

    paint.style = Paint.Style.FILL
    paint.textSize = 46f
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    canvas.drawText("30 cm", width * 0.38f, height * 0.21f, paint)
    canvas.drawText("15 cm", width * 0.8f, height * 0.42f, paint)
    canvas.drawText("8 cm", width * 0.28f, height * 0.72f, paint)

    paint.color = AndroidColor.rgb(27, 67, 50)
    paint.textSize = 52f
    canvas.drawText(product.name, 64f, height - 90f, paint)
    return bitmap
}

private fun drawProductShape(canvas: AndroidCanvas, paint: Paint, style: Int, w: Float, h: Float) {
    paint.style = Paint.Style.FILL
    when (style % 4) {
        0 -> {
            paint.color = AndroidColor.rgb(27, 67, 50)
            canvas.drawRoundRect(RectF(w * 0.18f, h * 0.56f, w * 0.82f, h * 0.64f), 28f, 28f, paint)
            paint.color = AndroidColor.rgb(185, 135, 67)
            canvas.drawRoundRect(RectF(w * 0.28f, h * 0.34f, w * 0.72f, h * 0.54f), 36f, 36f, paint)
            paint.color = AndroidColor.rgb(111, 79, 34)
            paint.strokeWidth = 24f
            canvas.drawLine(w * 0.34f, h * 0.64f, w * 0.22f, h * 0.86f, paint)
            canvas.drawLine(w * 0.66f, h * 0.64f, w * 0.78f, h * 0.86f, paint)
        }
        1 -> {
            paint.color = AndroidColor.argb(150, 244, 162, 97)
            canvas.drawCircle(w * 0.5f, h * 0.46f, w * 0.23f, paint)
            paint.color = AndroidColor.rgb(79, 52, 32)
            paint.strokeWidth = 18f
            canvas.drawLine(w * 0.5f, h * 0.12f, w * 0.5f, h * 0.34f, paint)
            paint.color = AndroidColor.rgb(183, 124, 53)
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(w * 0.24f, h * 0.32f, w * 0.76f, h * 0.6f), 70f, 70f, paint)
        }
        2 -> {
            paint.color = AndroidColor.rgb(185, 135, 67)
            canvas.drawRoundRect(RectF(w * 0.2f, h * 0.36f, w * 0.8f, h * 0.7f), 54f, 54f, paint)
            paint.color = AndroidColor.argb(120, 27, 67, 50)
            paint.strokeWidth = 10f
            repeat(5) { i ->
                canvas.drawLine(w * (0.25f + i * 0.1f), h * 0.38f, w * (0.3f + i * 0.08f), h * 0.68f, paint)
            }
        }
        else -> {
            paint.color = AndroidColor.rgb(185, 135, 67)
            canvas.drawRoundRect(RectF(w * 0.16f, h * 0.28f, w * 0.84f, h * 0.4f), 30f, 30f, paint)
            paint.color = AndroidColor.rgb(111, 79, 34)
            canvas.drawRoundRect(RectF(w * 0.22f, h * 0.45f, w * 0.78f, h * 0.55f), 30f, 30f, paint)
            paint.color = AndroidColor.rgb(185, 135, 67)
            canvas.drawRoundRect(RectF(w * 0.28f, h * 0.62f, w * 0.72f, h * 0.72f), 30f, 30f, paint)
        }
    }
}

@Composable
private fun Chip(text: String) {
    Text(
        text,
        color = ForestGreen,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Amber.copy(alpha = 0.22f))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun ScreenTitle(text: String) {
    Text(text, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black)
}

@Composable
private fun InputField(label: String, value: String, keyboardType: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ModeButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (selected) ForestGreen else OffWhite
    val fg = if (selected) Color.White else ForestGreen
    Box(
        modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontWeight = FontWeight.Bold)
    }
}

private fun Modifier.bambooPattern(): Modifier = drawBehind {
    repeat(12) { i ->
        val x = i * size.width / 9f - size.width * 0.2f
        drawLine(
            color = WarmCream.copy(alpha = 0.06f),
            start = Offset(x, -40f),
            end = Offset(x + size.width * 0.45f, size.height + 40f),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HastaShilpaPreview() {
    Hasta_shilpa_v1Theme(dynamicColor = false) {
        HastaShilpaApp()
    }
}
