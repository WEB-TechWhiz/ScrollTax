package com.scrolltax.app.ui.tax

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrolltax.domain.CalculateProgressiveTaxUseCase
import com.scrolltax.app.ui.theme.Primary
import com.scrolltax.app.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxBracketScreen(
    calculateProgressiveTaxUseCase: CalculateProgressiveTaxUseCase,
    onBack: () -> Unit
) {
    var incomeText by remember { mutableStateOf(TextFieldValue("")) }
    var taxResult by remember { mutableStateOf<Double?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tax Bracket Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = incomeText,
                onValueChange = { incomeText = it },
                label = { Text("Annual Income") },
                placeholder = { Text("e.g., 85000") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                },
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Primary,
                    cursorColor = Primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val income = incomeText.text.replace(",", "").toDoubleOrNull()
                    if (income == null) {
                        errorMessage = "Please enter a valid number"
                        taxResult = null
                    } else {
                        errorMessage = null
                        taxResult = calculateProgressiveTaxUseCase.calculate(income)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Tax", color = Color.White, fontSize = 16.sp)
            }

            errorMessage?.let { err ->
                Text(text = err, color = MaterialTheme.colorScheme.error)
            }

            taxResult?.let { tax ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Calculated Tax:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "$$${"%.2f".format(tax)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Primary,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}
