import requests
import csv

# name;id;region;region_code;city;lat;lon

base_url_stations = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/elencoStazioni/"

urls = [base_url_stations + str(i) for i in range(0,23)]

print "#Urls", len(urls)


def regionNameFromCode(code):
    if (code == 1 ): return "Lombardia"
    if (code == 2 ): return "Liguria"
    if (code == 3 ): return "Piemonte"
    if (code == 4 ): return "Valle d'Aosta"
    if (code == 5 ): return "Lazio"
    if (code == 6 ): return "Umbria"
    if (code == 7 ): return "Molise"
    if (code == 8 ): return "Emilia Romagna"
    if (code == 9 ): return "Trentino Alto Adige"
    if (code == 10): return "Friuli Venezia Giulia"
    if (code == 11): return "Marche"
    if (code == 12): return "Veneto"
    if (code == 13): return "Toscana"
    if (code == 14): return "Sicilia"
    if (code == 15): return "Basilicata"
    if (code == 16): return "Puglia"
    if (code == 17): return "Calabria"
    if (code == 18): return "Campania"
    if (code == 19): return "Abruzzo"
    if (code == 20): return "Sardegna"
    if (code == 21): return "Trentino Alto Adige"
    if (code == 22): return "Trentino Alto Adige"


stations = {}
for url in urls:
    print "Requesting url", url
    r = requests.get(url)
    data = r.json()
    for rowdata in data:

        url_reg = "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/regione/"+rowdata["codStazione"]
        try:
            reg_code = requests.get(url_reg).json()
            # Per qualche strano motivo, il Trentino alto adige ha 3 possibili codice regione
            if (reg_code == 21 or reg_code == 22):
                reg_code = 9

            row = [
                rowdata["localita"]["nomeLungo"],
                rowdata["codStazione"],
                regionNameFromCode(reg_code),
                reg_code,
                rowdata["nomeCitta"],
                rowdata["lat"],
                rowdata["lon"] ]
            stations[rowdata["codStazione"]] = row
        except ValueError:
            print "ERRORE con url"
            print url_reg
            row = [
                rowdata["localita"]["nomeLungo"],
                rowdata["codStazione"],
                'N/A',
                0,
                'N/A',
                0,
                0 ]
            stations[rowdata["codStazione"]] = row

with open('stations.csv', 'wb') as csvfile:
    writer = csv.writer(csvfile, delimiter=';', quoting=csv.QUOTE_NONE)
    for (k,v) in stations.iteritems():
        writer.writerow(v)
