package com.example.walletapp.loan


import android.content.Context
import org.json.JSONArray
import org.json.JSONObject



object LoanPersistence {



    private const val FILE_NAME = "loans.json"



    fun save(

        context: Context,

        loans: List<Loan>

    ){


        val array = JSONArray()


        loans.forEach { loan ->


            val obj = JSONObject()


            obj.put("id", loan.id)

            obj.put("name", loan.name)

            obj.put("amount", loan.amount)

            obj.put("interestRate", loan.interestRate)

            obj.put("installmentCount", loan.installmentCount)

            obj.put("installmentInterval", loan.installmentInterval)

            obj.put("monthlyPayment", loan.monthlyPayment)

            obj.put("firstInstallmentDate", loan.firstInstallmentDate)
            obj.put("receiveDate", loan.receiveDate)



            array.put(obj)


        }



        context.openFileOutput(
            FILE_NAME,
            Context.MODE_PRIVATE
        ).use {

            it.write(array.toString().toByteArray())

        }


    }




    fun load(

        context: Context

    ): List<Loan>{


        return try {


            val text =
                context.openFileInput(FILE_NAME)
                    .bufferedReader()
                    .readText()



            val array =
                JSONArray(text)



            val result =
                mutableListOf<Loan>()



            for(i in 0 until array.length()){


                val obj =
                    array.getJSONObject(i)



                result.add(

                    Loan(

                        id = obj.getString("id"),

                        name = obj.getString("name"),

                        amount = obj.getLong("amount"),

                        interestRate =
                            obj.getDouble("interestRate"),

                        installmentCount =
                            obj.getInt("installmentCount"),

                        installmentInterval =
                            obj.getInt("installmentInterval"),

                        monthlyPayment =
                            obj.getLong("monthlyPayment"),

                        firstInstallmentDate =
                            obj.getString("firstInstallmentDate"),
                        receiveDate =
                            obj.getString("receiveDate")
                    )

                )


            }



            result


        } catch(e: Exception){


            emptyList()

        }


    }



}
