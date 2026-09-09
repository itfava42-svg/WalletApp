package com.example.walletapp.loan


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontWeight



@Composable
fun LoanDetailScreen(
    loan: Loan
){


    val installments =
        LoanScheduleGenerator.generate(loan)



    Column(

        modifier =
        Modifier
            .fillMaxSize()
            .background(MatureColors.Surface)
            .padding(20.dp)

    ){


        Card(

            modifier =
            Modifier.fillMaxWidth(),

            shape =
            RoundedCornerShape(18.dp),

            colors =
            CardDefaults.cardColors(
                containerColor=MatureColors.Primary
            )

        ){


            Column(

                modifier=
                Modifier.padding(20.dp)

            ){


                Text(

                    text=loan.name,

                    color=Color.White,

                    fontSize=22.sp,

                    fontWeight=FontWeight.Bold

                )


                Spacer(
                    Modifier.height(12.dp)
                )


                Text(

                    text="مبلغ: ${loan.amount} ریال",

                    color=Color.White

                )


                Text(

                    text="قسط ماهانه: ${loan.monthlyPayment} ریال",

                    color=Color.White

                )


                Text(

                    text="تعداد اقساط: ${loan.installmentCount}",

                    color=Color.White

                )


            }

        }



        Spacer(
            Modifier.height(20.dp)
        )



        Text(

            text="برنامه اقساط",

            color=MatureColors.Primary,

            fontSize=18.sp,

            fontWeight=FontWeight.Bold

        )


        Spacer(
            Modifier.height(12.dp)
        )



        LazyColumn(

            verticalArrangement =
            Arrangement.spacedBy(10.dp)

        ){


            items(installments){ installment ->


                InstallmentCard(
                    installment
                )


            }


        }



    }


}




@Composable
private fun InstallmentCard(

    installment: LoanInstallment

){


    Card(

        modifier =
        Modifier.fillMaxWidth(),

        shape =
        RoundedCornerShape(16.dp),

        colors =
        CardDefaults.cardColors(
            containerColor=Color.White
        )

    ){


        Row(

            modifier=
            Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment =
            Alignment.CenterVertically

        ){


            Icon(

                Icons.Default.Payment,

                contentDescription=null,

                tint=MatureColors.Secondary

            )


            Spacer(
                Modifier.width(12.dp)
            )


            Column(

                modifier=
                Modifier.weight(1f)

            ){


                Text(

                    text="قسط شماره ${installment.number}",

                    fontWeight=FontWeight.Bold,

                    color=MatureColors.Primary

                )


                Text(

                    text="تاریخ: ${installment.dueDate}",

                    color=MatureColors.TextSecondary

                )


                Text(

                    text="${installment.amount} ریال",

                    color=MatureColors.TextPrimary

                )


            }



            Button(

                onClick={

                    // مرحله ۶ وصل می‌شود

                }

            ){


                Icon(

                    Icons.Default.CheckCircle,

                    null

                )


                Spacer(
                    Modifier.width(4.dp)
                )


                Text(
                    "پرداخت"
                )


            }


        }


    }


}
