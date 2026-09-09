package com.example.walletapp.loan


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontWeight



@Composable
fun LoanRegistrationScreen(){


    var name by remember { mutableStateOf("") }

    var amount by remember { mutableStateOf("") }

    var interest by remember { mutableStateOf("") }

    var count by remember { mutableStateOf("") }

    var installmentInterval by remember { mutableStateOf("1") }

    var receiveDate by remember { mutableStateOf("") }

    var firstInstallmentDate by remember { mutableStateOf("") }

    var guarantee by remember { mutableStateOf("") }



    Column(

        modifier =
        Modifier
            .fillMaxSize()
            .background(MatureColors.Surface)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)

    ){



        Card(

            modifier = Modifier.fillMaxWidth(),

            colors =
            CardDefaults.cardColors(
                containerColor = MatureColors.Primary
            ),

            shape =
            RoundedCornerShape(18.dp)

        ){

            Column(
                modifier =
                Modifier.padding(24.dp)
            ){

                Text(
                    text="📋 ثبت وام جدید",
                    color=Color.White,
                    fontSize=24.sp,
                    fontWeight=FontWeight.Bold
                )


                Text(
                    text="اطلاعات وام را وارد کنید",
                    color=Color.White.copy(alpha=.8f)
                )

            }

        }



        Spacer(
            Modifier.height(20.dp)
        )



        Card(

            modifier=Modifier.fillMaxWidth(),

            colors =
            CardDefaults.cardColors(
                containerColor = Color.White
            ),

            shape =
            RoundedCornerShape(18.dp)

        ){


            Column(
                modifier=
                Modifier.padding(20.dp)
            ){


                Text(
                    text="اطلاعات وام",
                    color=MatureColors.Primary,
                    fontWeight=FontWeight.Bold
                )



                Spacer(
                    Modifier.height(12.dp)
                )



                OutlinedTextField(

                    value=name,

                    onValueChange={
                        name=it
                    },

                    modifier=Modifier.fillMaxWidth(),

                    label={
                        Text("نام وام")
                    },

                    leadingIcon={

                        Icon(
                            Icons.Default.AccountBalance,
                            null,
                            tint=MatureColors.Secondary
                        )

                    }

                )



                Spacer(
                    Modifier.height(12.dp)
                )



                OutlinedTextField(

                    value=amount,

                    onValueChange={
                        amount=it
                    },

                    modifier=Modifier.fillMaxWidth(),

                    label={
                        Text("مبلغ وام")
                    }

                )



                Spacer(
                    Modifier.height(12.dp)
                )



                OutlinedTextField(

                    value=interest,

                    onValueChange={
                        interest=it
                    },

                    modifier=Modifier.fillMaxWidth(),

                    label={
                        Text("درصد سود")
                    }

                )



                Spacer(
                    Modifier.height(12.dp)
                )



                OutlinedTextField(

                    value=count,

                    onValueChange={
                        count=it
                    },

                    modifier=Modifier.fillMaxWidth(),

                    label={
                        Text("تعداد اقساط")
                    }

                )

                Spacer(
                    Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value=installmentInterval,
                    onValueChange={
                        installmentInterval=it
                    },
                    modifier=Modifier.fillMaxWidth(),
                    label={
                        Text("فاصله اقساط (ماه)")
                    }
                )



                Spacer(
                    Modifier.height(20.dp)
                )

                OutlinedTextField(
                    value=receiveDate,
                    onValueChange={
                        receiveDate=it
                    },
                    modifier=Modifier.fillMaxWidth(),
                    label={
                        Text("تاریخ دریافت وام")
                    }
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value=firstInstallmentDate,
                    onValueChange={
                        firstInstallmentDate=it
                    },
                    modifier=Modifier.fillMaxWidth(),
                    label={
                        Text("تاریخ اولین قسط")
                    }
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value=guarantee,
                    onValueChange={
                        guarantee=it
                    },
                    modifier=Modifier.fillMaxWidth(),
                    label={
                        Text("نوع ضمانت")
                    }
                )

                Spacer(
                    Modifier.height(20.dp)
                )



                Button(

                    modifier=
                    Modifier.fillMaxWidth(),
                    onClick={

                        val loanAmount = amount.toLongOrNull() ?: 0L
                        val loanInterest = interest.toDoubleOrNull() ?: 0.0
                        val installmentNumber = count.toIntOrNull() ?: 0
                        val interval = installmentInterval.toIntOrNull() ?: 1

                        val monthlyPayment = LoanCalculator.calculateMonthlyPayment(
                            amount = loanAmount,
                            interestRate = loanInterest,
                            installmentCount = installmentNumber
                        )

                        val loan = Loan(
                            name = name,
                            amount = loanAmount,
                            interestRate = loanInterest,
                            installmentCount = installmentNumber,
                            installmentInterval = interval,
                            monthlyPayment = monthlyPayment,
                            receiveDate = receiveDate,
                            firstInstallmentDate = firstInstallmentDate,
                            guarantee = guarantee
                        )

                        LoanStore.addLoan(loan)
                    }

                ){

                    Icon(
                        Icons.Default.CheckCircle,
                        null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )


                    Text(
                        "ثبت وام"
                    )


                }


            }

        }


    }


}
