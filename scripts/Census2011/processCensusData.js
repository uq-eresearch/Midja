var csv = require('csv');
var fs = require('fs');

// process Census Data for mapping via AURIN portal

// 2001 Indigenous Percentage of population
csv()
.from.stream(fs.createReadStream(__dirname+'/2011 DataPacks BCP IP TSP Release 3/2011 Time Series Profile Release 3/Long Descriptor/LGA/AUST/2011Census_T06A_AUST_LGA_long.csv'), {columns: true})
.to.path(__dirname+'/processed/LGA2001IndigenousPercentage.csv', {columns: ["region_id","2001_ind_percent_of_population"], header:true})
.transform( function(row){
	// remove LGA prefix from region id
	row["region_id"] = row["region_id"].substring(3);
	// calculate indigenous persons as % of total population of region
	var numIndPersons = row["2001_Census_Total_Total_Persons"] - row["2001_Census_Total_Non_Indigenous_Persons"];
	row["2001_ind_percent_of_population"] = numIndPersons / row["2001_Census_Total_Total_Persons"] * 100;
	return row;
})
.on('error', function(error){
  console.log(error.message);
});

// 2006 Indigenous Percentage of population
csv()
.from.stream(fs.createReadStream(__dirname+'/2011 DataPacks BCP IP TSP Release 3/2011 Time Series Profile Release 3/Long Descriptor/LGA/AUST/2011Census_T06B_AUST_LGA_long.csv'), {columns: true})
.to.path(__dirname+'/processed/LGA2006IndigenousPercentage.csv', {columns: ["region_id","2006_ind_percent_of_population"], header:true})
.transform( function(row){
	// remove LGA prefix from region id
	row["region_id"] = row["region_id"].substring(3);
	// calculate indigenous persons as % of total population of region
	var numIndPersons = row["2006_Census_Total_Total_Persons"] - row["2006_Census_Total_Non_Indigenous_Persons"];
	row["2006_ind_percent_of_population"] = numIndPersons / row["2006_Census_Total_Total_Persons"] * 100;
	return row;
})
.on('error', function(error){
  console.log(error.message);
});

// 2011 Indigenous Percentage of population
csv()
.from.stream(fs.createReadStream(__dirname+'/2011 DataPacks BCP IP TSP Release 3/2011 Time Series Profile Release 3/Long Descriptor/LGA/AUST/2011Census_T06C_AUST_LGA_long.csv'), {columns: true})
.to.path(__dirname+'/processed/LGA2011IndigenousPercentage.csv', {columns: ["region_id","2011_ind_percent_of_population"], header:true})
.transform( function(row){
	// remove LGA prefix from region id
	row["region_id"] = row["region_id"].substring(3);
	// calculate indigenous persons as % of total population of region
	var numIndPersons = row["2011_Census_Total_Total_Persons"] - row["2011_Census_Total_Non_Indigenous_Persons"];
	row["2011_ind_percent_of_population"] = numIndPersons / row["2011_Census_Total_Total_Persons"] * 100;
	return row;
})
.on('error', function(error){
  console.log(error.message);
});