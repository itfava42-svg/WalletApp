package com.example.walletapp.loan


import android.content.Context
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import java.util.UUID



object LoanPaymentManager {



    fun payInstallment(

        context: Context,

        loan: Loan,

        installment: LoanInstallment

    ): LoanInstallment {



        TransactionStore.attachContext(context)



        val transaction = Transaction(

            id = UUID.randomUUID().toString(),

            title = "پرداخت قسط ${loan.name}",

            subtitle = "قسط شماره ${installment.number}",

            amount = installment.amount,

            type = TransactionType.EXPENSE,

            iconKey = "وام",

            tags = listOf("وام"),

            description =
                "پرداخت قسط ${loan.name}"

        )



        TransactionStore.addManual(transaction)



        return installment.copy(

            isPaid = true,

            paidDate = "امروز",

            transactionId = transaction.id

        )

    }



}
