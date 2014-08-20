var csv = require('csv');
var fs = require('fs');

csv()
.from.stream(fs.createReadStream(__dirname+'/HousingReferences.csv'), {columns: true})
.transform( function(row,index){
  var result = "TY  - Generic\n";
  result += "T1  - " + row.Title + "\n";
  if (row.Year){
    result += "Y1  - " + row.Year + "///\n";
  }
  result += "AU - " + row.Author + "\n";
  if (row["AERC BIB38 Classification"] && row["AERC BIB38 Classification"].indexOf("N/A") == -1){
    result += "KW  - " + row["AERC BIB38 Classification"] + "\n";
  }
  if (row["AERC Region Classification"] && row["AERC Region Classification"].indexOf("N/A") == -1){
    result += "KW  - " + row["AERC Region Classification"] + "\n";
  }
  if (row["AERC Subject Classification"] && row["AERC Subject Classification"].indexOf("N/A") == -1){
    result += "KW  - " + row["AERC Subject Classification"] + "\n";
  }
  if (row["Settlement Classification"] && row["Settlement Classification"].indexOf("N/A") == -1){
    result += "KW  - " + row["Settlement Classification"] + "\n";
  }
  if (row["Remoteness Classification"] && row["Remoteness Classification"].indexOf("N/A") == -1) {
    result += "KW  - " + row["Remoteness Classification"] + "\n";
  }
  if (row["Keywords themes"] && row["Keywords themes"].indexOf("N/A") == -1){
    result += "KW  - " + row["Keywords themes"] + "\n";
  }
  if (row.SubGroup && row.SubGroup.indexOf("N/A") == -1){
    result += "KW  - " + row.SubGroup + "\n";    
  }
  result += "KW  - Generated\n";

  if (row["ISBN ISSN"]) {
    result += "SN  - " + row["ISBN ISSN"] + "\n";
  }
  var abstract = "";
  if (row.Summary) {
    abstract += row.Summary.replace(/\r\n|\n/g, " ");
  }
  if (row["Other Bibliographic details"]){
    abstract +=  row["Other Bibliographic details"] ;
  }
  
  
  var notes = "";
  if (row["Author Background"]) {
    notes += "Author background: " + row["Author Background"] + "; ";
  }
  if (row["Language group"] && row["Language group"] != "N/A") {
    notes += "Language group: " + row["Language group"] + "; ";
  }
  if (row["Research Methods"]) {
    notes += "Research Methods: " + row["Research Methods"] + "; ";
  }
  if (row["Place"] && row["Place"] !="N/A") {
    notes += "Place: " + row["Place"] + "; ";
  }
  result += "N1  - " + notes + "\n";
  if (abstract) {
    result += "N2  - " + abstract + "\n";
  }
  result += "ER  - \n"
  console.log(index + " " + result);
  fs.writeFile(__dirname+"/tmp/"+index+".ris", result, function(err) {
    if(err) {
        console.log(err);
    }
  }); 
})
.on('end', function(count){
  console.log('Number of lines: '+count);
})
.on('error', function(error){
  console.log(error.message);
});