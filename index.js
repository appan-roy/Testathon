const con = require('./db_connection')
const express = require('express')
const app = express()

app.get('/products', (req, res) => {
    con.query('SELECT * FROM testathon.products;', (err, result) => {
        if(err){
            res.send('Error')
        }else{
            res.send(result)
        }
    })
}).listen(4000)