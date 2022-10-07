import xlsxwriter
import argparse
import json
from typing import NamedTuple
from collections import Counter

parser = argparse.ArgumentParser(description='Transform JSON to XLSX')
parser.add_argument("-f", "--file", required=True)

ROW_OFFSET = 2


def row_index(row):
    return row - ROW_OFFSET


class ColumnLayout(NamedTuple):
    description: str
    idx: int
    width: int


index_col = ColumnLayout('Nummer', idx=0, width=6)
pvs_manufacturer_col = ColumnLayout('PVS-Hersteller', idx=1, width=19)
name_col = ColumnLayout('Beschreibung', idx=2, width=40)
freetext_col = ColumnLayout('Freitext', idx=name_col.idx, width=name_col.width)
ingredient_col = ColumnLayout('Ingredient', idx=name_col.idx, width=name_col.width)
pzn_col = ColumnLayout('PZN/Typ', idx=3, width=10)
prescription_type_col = ColumnLayout('Typ', idx=pzn_col.idx, width=pzn_col.width)
note_col = ColumnLayout('Hinweis', idx=4, width=35)
insurance_col = ColumnLayout('Krankenkasse', idx=5, width=20)
prescription_id_col = ColumnLayout('tid', idx=6, width=25)
access_code_col = ColumnLayout('ac', idx=7, width=65)
data_matrix_code_col = ColumnLayout('DMC', idx=8, width=110)
avs_manufacturer_col = ColumnLayout('AVS-Hersteller', idx=9, width=15)
claimed_col = ColumnLayout('erfolgreiche geclaimed', idx=10, width=20)
comment_col = ColumnLayout('Kommentar (ggf. fachliche Fehler)', idx=11, width=30)
status_col = ColumnLayout('weiterführender Status', idx=12, width=25)
empty_col = ColumnLayout('', idx=13, width=2)
arz_col = ColumnLayout('ARZ', idx=14, width=15)
arz_provision_path_col = ColumnLayout('Bereitstellungsweg ARZ', idx=15, width=25)
processable_col = ColumnLayout('Verarbeitbarkeit', idx=16, width=25)
invoice_assessment_col = ColumnLayout('Einschätzung Abrechenbarkeit', idx=17, width=35)
reject_reason_col = ColumnLayout('Rückweisungsgründe', idx=18, width=35)
coordination_col = ColumnLayout('In Abstimmung zwischen den technisch beteiligten lösbar (ja/nein). \
Nein: hat Auswirkungen auf Patient, Arzt/Apotheker',
                                idx=19, width=100)

# columns coming from the prescriptions
prescription_columns = [index_col, pvs_manufacturer_col, prescription_id_col, access_code_col, pzn_col,
                        prescription_type_col,
                        name_col, freetext_col, ingredient_col, note_col, insurance_col, data_matrix_code_col]

# columns to be filled out during the konnekathon
kthon_columns = [avs_manufacturer_col, claimed_col, comment_col, status_col, empty_col, arz_col,
                 arz_provision_path_col, processable_col, invoice_assessment_col, reject_reason_col,
                 coordination_col]

all_columns = prescription_columns + kthon_columns

highlight_columns = [pvs_manufacturer_col, avs_manufacturer_col, arz_col]


class Prescription:

    def __init__(self, source):
        self.source = source

    @property
    def prescription_id(self):
        return self.source['prescriptionId']

    @property
    def access_code(self):
        return self.source['accessCode']

    @property
    def data_matrix_code(self):
        return json.dumps({'urls': [f"Task/{self.prescription_id}/$accept?ac={self.access_code}"]})

    @property
    def name(self):
        return self.source['medication']['name']

    @property
    def display_name(self):
        if self.type == 'freitext':
            return self.source['medication']['freeText']
        elif self.type == 'wirkstoff' or self.type == "rezeptur":
            return f"{self.source['medication']['ingredient']} {self.source['medication']['ingredientStrength']}"
        else:
            return self.name

    @property
    def pzn(self):
        return self.source['medication']['pzn']

    def has_pzn(self):
        return self.pzn is not None

    @property
    def medication_note(self):
        return self.source['medication']['note']

    @property
    def insurance_name(self):
        return self.source['coverage']['insuranceName']

    @property
    def ingredient(self):
        return f"{self.source['medication']['ingredient']} {self.source['medication']['ingredientStrength']}"

    @property
    def freetext(self):
        return self.source['medication']['freeText']

    @property
    def type(self):
        return self.source['medication']['type']

    def write_prescription(self, worksheet, row):
        worksheet.write(row, index_col.idx, row_index(row))
        worksheet.write(row, prescription_id_col.idx, self.prescription_id)
        worksheet.write(row, access_code_col.idx, self.access_code)

        if self.has_pzn():
            worksheet.write(row, pzn_col.idx, self.pzn)
            worksheet.write(row, name_col.idx, self.display_name)
        else:
            worksheet.write(row, prescription_type_col.idx, self.type)
            if self.type == 'freitext':
                worksheet.write(row, freetext_col.idx, self.freetext)
            elif self.type == 'wirkstoff' or self.type == "rezeptur":
                worksheet.write(row, ingredient_col.idx, self.ingredient)

        worksheet.write(row, note_col.idx, self.medication_note)
        worksheet.write(row, insurance_col.idx, self.insurance_name)
        worksheet.write(row, data_matrix_code_col.idx, self.data_matrix_code)

    def __eq__(self, other):
        return self.name == other.name

    def __lt__(self, other):
        return self.display_name < other.display_name


def process(file_name):
    print("Processing: " + file_name)
    out_file_name = file_name.replace('.json', '.xlsx')

    with open(file_name) as file:
        json_content = json.loads(file.read())

    workbook = xlsxwriter.Workbook(out_file_name)
    worksheet = workbook.add_worksheet()
    row = create_header_rows(workbook, worksheet)

    border_format = workbook.add_format({'border': 1, 'border_color': '#000000', 'bg_color': '#E0E0EF'})

    prescriptions = [Prescription(p) for p in json_content]
    prescriptions.sort()

    for p in prescriptions:
        row += 1
        p.write_prescription(worksheet, row)
        # now format the columns which will be filled during konnektathon
        for c in kthon_columns:
            worksheet.write(row, c.idx, None, border_format)

    summary_sheet = workbook.add_worksheet('Summary')
    summary_sheet.write('A1', "PZN")
    summary_sheet.write('A2', "Freitext")
    summary_sheet.write('A3', "Wirkstoff")
    summary_sheet.write('A4', "Rezeptur")

    prescription_types = Counter([p.type for p in prescriptions])
    summary_sheet.write('B1', prescription_types[None])
    summary_sheet.write('B2', prescription_types['freitext'])
    summary_sheet.write('B3', prescription_types['wirkstoff'])
    summary_sheet.write('B4', prescription_types['rezeptur'])

    chart = workbook.add_chart({'type': 'pie'})
    chart.set_title({'name': 'Rezept-Typen'})
    chart.add_series({'values': '=Summary!$B$1:$B$4', 'categories': '=Summary!$A$1:$A$4'})
    summary_sheet.insert_chart('D3', chart)

    workbook.close()


def create_header_rows(workbook, worksheet):
    title_format = workbook.add_format({'bold': True, 'align': 'center', 'valign': 'top'})
    header_format = workbook.add_format(
        {'bg_color': '#C0C0C0', 'border': 1, 'border_color': '#000000', 'align': 'center'})
    header_format_bold = workbook.add_format(
        {'bg_color': '#C0C0C0', 'border': 1, 'border_color': '#000000', 'bold': True, 'align': 'center'})

    worksheet.merge_range("A1:B2", "XX. Konnektathon Testsession", cell_format=title_format)
    row = ROW_OFFSET

    for c in all_columns:
        hf = header_format_bold if c in highlight_columns else header_format
        set_header(worksheet, row, c, hf)

    return row


def set_header(worksheet, row, col: ColumnLayout, header_format):
    worksheet.write(row, col.idx, col.description, header_format)
    worksheet.set_column(col.idx, col.idx, col.width)


if __name__ == '__main__':
    args = parser.parse_args()
    process(args.file)
