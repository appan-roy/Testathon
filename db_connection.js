var mysql2 = require('mysql2')

var con = mysql2.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'Royapp.23'
})

con.connect((err) => {
    if(err){
        throw err
    }
    console.log('Successfully connected')
})

module.exports = con