package com.example.walletapp.loan


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Money

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.graphics.Color



@Composable
fun LoanListScreen(){


    val loans = LoanStore.getLoans()



    Column(

        modifier =
        Modifier
            .fillMaxSize()
            .background(MatureColors.Surface)
            .padding(20.dp)

    ){


        Text(

            text="وام‌های من",

            fontSize=24.sp,

            fontWeight=FontWeight.Bold,

            color=MatureColors.Primary

        )



        Spacer(
            Modifier.height(20.dp)
        )



        if(loans.isEmpty()){


            Card(

                modifier=Modifier.fillMaxWidth(),

                shape=RoundedCornerShape(18.dp),

                colors=
                CardDefaults.cardColors(
                    containerColor=Color.White
                )

            ){

                Text(

                    text="هنوز وامی ثبت نشده است",

                    modifier=
                    Modifier.padding(24.dp),

                    color=MatureColors.TextSecondary

                )


            }


        }else{


            LazyColumn(

                verticalArrangement =
                Arrangement.spacedBy(12.dp)

            ){


                items(loans){ loan ->


                    LoanCard(loan)



                }


            }


        }



    }


}



@Composable
private fun LoanCard(
    loan: Loan
){


    Card(

        modifier =
        Modifier.fillMaxWidth(),

        shape =
        RoundedCornerShape(18.dp),

        colors =
        CardDefaults.cardColors(

            containerColor = Color.White

        ),

        elevation =
        CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )

    ){



        Column(

            modifier =
            Modifier.padding(20.dp)

        ){



            Row(

                verticalAlignment =
                Alignment.CenterVertically

            ){


                Icon(

                    Icons.Default.AccountBalance,

                    contentDescription=null,

                    tint=MatureColors.Secondary

                )


                Spacer(
                    Modifier.width(10.dp)
                )


                Text(

                    text=loan.name,

                    fontSize=18.sp,

                    fontWeight=FontWeight.Bold,

                    color=MatureColors.Primary

                )


            }



            Spacer(
                Modifier.height(16.dp)
            )



            LoanInfoRow(

                icon=Icons.Default.Money,

                title="مبلغ وام",

                value="${loan.amount} ریال"

            )



            LoanInfoRow(

                icon=Icons.Default.CalendarMonth,

                title="اقساط",

                value="${loan.installmentCount} ماه"

            )



            Spacer(
                Modifier.height(12.dp)
            )



            Divider()



            Spacer(
                Modifier.height(12.dp)
            )


            Text(

                text=
                if(loan.isActive)
                    "وضعیت: فعال"
                else
                    "وضعیت: پایان یافته",

                color=
                if(loan.isActive)
                    MatureColors.Success
                else
                    MatureColors.TextHint,

                fontWeight=FontWeight.Bold

            )



        }


    }


}



@Composable
private fun LoanInfoRow(

    icon: androidx.compose.ui.graphics.vector.ImageVector,

    title:String,

    value:String

){


    Row(

        modifier=
        Modifier.fillMaxWidth(),

        verticalAlignment=
        Alignment.CenterVertically

    ){


        Icon(

            imageVector=icon,

            contentDescription=null,

            tint=MatureColors.PrimaryLight,

            modifier=
            Modifier.size(20.dp)

        )


        Spacer(
            Modifier.width(8.dp)
        )


        Text(

            text=title,

            color=MatureColors.TextSecondary

        )


        Spacer(
            Modifier.weight(1f)
        )


        Text(

            text=value,

            color=MatureColors.TextPrimary,

            fontWeight=FontWeight.SemiBold

        )


    }


}
