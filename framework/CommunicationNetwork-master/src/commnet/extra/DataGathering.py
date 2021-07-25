import mysql.connector
from mysql.connector import Error
import configparser
from enum import Enum
import csv


class METRICS(Enum):
    ms_id = 0
    project_id = 1
    number_of_files = 2
    number_of_conflicted_files = 3
    number_chunks = 4  # #C
    number_conflicted_chunks = 5  # #C2
    number_developers = 6  # #Dev
    conflict_code_churn = 7  # #CLoC
    code_complexity_merge_code = 8  # #C4
    dev_has_knowledge = 9  # %CIK
    change_type = 10  # PFC3
    time_to_resolve = 11  # TimeDiff


# returns array with smaller time for merge conflict
def get_array_with_smaller_time_from_records(array):
    rows = len(array)
    columns = len(array[0]) - 1
    filteredArray = [[0] * columns for i in range(rows)]
    for index, element in enumerate(array):
        left_merge_time_diff, right_merge_time_diff = element[columns-1:]
        smallerTime = left_merge_time_diff if left_merge_time_diff < right_merge_time_diff else right_merge_time_diff
        filteredArray[index][METRICS.ms_id.value] = element[METRICS.ms_id.value]
        filteredArray[index][METRICS.project_id.value] = element[METRICS.project_id.value]
        filteredArray[index][METRICS.number_of_files.value] = element[METRICS.number_of_files.value]
        filteredArray[index][METRICS.number_of_conflicted_files.value] = element[METRICS.number_of_conflicted_files.value]
        filteredArray[index][METRICS.number_chunks.value] = element[METRICS.number_chunks.value]
        filteredArray[index][METRICS.number_conflicted_chunks.value] = element[METRICS.number_conflicted_chunks.value]
        filteredArray[index][METRICS.number_developers.value] = element[METRICS.number_developers.value]
        filteredArray[index][METRICS.conflict_code_churn.value] = element[METRICS.conflict_code_churn.value]
        filteredArray[index][METRICS.code_complexity_merge_code.value] = element[METRICS.code_complexity_merge_code.value]
        filteredArray[index][METRICS.dev_has_knowledge.value] = element[METRICS.dev_has_knowledge.value]
        filteredArray[index][METRICS.change_type.value] = 0 if element[METRICS.change_type.value] == 'OT' else 1  # use binary for change_type
        filteredArray[index][METRICS.time_to_resolve.value] = smallerTime

    return filteredArray


# merge the metrics if the ms_id is same.
# for example take all the developer knowledge of merge scenario and get percentage,
def get_combined_columns_data(array, index, count):
    code_complexity_merge_code = 0
    dev_has_knowledge = 0
    change_type = 0
    for x in range(index, index + count):
        dev_has_knowledge += array[x][METRICS.dev_has_knowledge.value]
        change_type += array[x][METRICS.change_type.value]
        code_complexity_merge_code += array[x][METRICS.code_complexity_merge_code.value]

    dev_has_knowledge = (dev_has_knowledge / count) if dev_has_knowledge > 0 else dev_has_knowledge
    change_type = (change_type / count) if change_type > 0 else change_type
    return code_complexity_merge_code, dev_has_knowledge, change_type


# returns merge scenario by merging merge conflicts in a single merge scenario
# for example if we have two rows of same ms_id, we merge them together.
def get_merged_data_per_merge_scenario(array):
    arrayCombined = [[0] * len(array[0]) for i in range(len(array))]
    previous_ms_id = 0
    for index, element in enumerate(array):
        if previous_ms_id == element[0]:
            continue

        counter = 0
        for x in range(index, len(array)):
            # if merge scenarios are same as previous one.
            if element[0] == array[x][0]:
                counter += 1  # count number of merge scenarios with same ms_id (to combine them later)
            else:
                previous_ms_id = element[0]
                code_complexity_merge_code, dev_has_knowledge, change_type = get_combined_columns_data(array, index, counter)
                arrayCombined[index][METRICS.ms_id.value] = element[METRICS.ms_id.value]
                arrayCombined[index][METRICS.project_id.value] = element[METRICS.project_id.value]
                arrayCombined[index][METRICS.number_of_files.value] = element[METRICS.number_of_files.value]
                arrayCombined[index][METRICS.number_of_conflicted_files.value] = element[METRICS.number_of_conflicted_files.value]
                arrayCombined[index][METRICS.number_chunks.value] = element[METRICS.number_chunks.value]
                arrayCombined[index][METRICS.number_conflicted_chunks.value] = element[METRICS.number_conflicted_chunks.value]
                arrayCombined[index][METRICS.number_developers.value] = element[METRICS.number_developers.value]
                arrayCombined[index][METRICS.conflict_code_churn.value] = element[METRICS.conflict_code_churn.value]
                arrayCombined[index][METRICS.code_complexity_merge_code.value] = code_complexity_merge_code
                arrayCombined[index][METRICS.dev_has_knowledge.value] = dev_has_knowledge
                arrayCombined[index][METRICS.change_type.value] = change_type
                arrayCombined[index][METRICS.time_to_resolve.value] = element[METRICS.time_to_resolve.value]
                break

    arrayCombinedWithoutZeros = [x for x in arrayCombined if x[0] != 0]  # remove all the zeros from array.
    return arrayCombinedWithoutZeros

# save metrics in csv file
# rewrites the metrics in csv file, does not append.
def create_csv_file_of_merge_scenarios_metrics(array):
    csv_columns = [['ms_id', 'project_id', '#files', '#ConfFiles', '#C', '#C2', '#Dev', '#CLoC', 'C4', '%CIK', 'PFC3', 'TimeDiff']]

    with open('merge_scenarios_metrics.csv', 'w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerows(csv_columns)
        writer.writerows(array)

    csv_file.close()


def get_data_from_database():
    sql_select_query = """select merge_scenarios.id as ms_id, merge_scenarios.project_id as project_id,
                          ms_metrics.number_of_files, ms_metrics.number_conflicted_files,
                          ms_metrics.number_chunks, ms_metrics.number_conflicted_chunks,
                          ms_metrics.number_developers, ms_metrics.conflict_code_churn,
                          merge_conflict_metrics.cyclomatic_complexity as code_complexity_merge_code,
                          merge_conflict_metrics.dev_has_knowledge, merge_conflict_metrics.change_type,
                          merge_conflict_info.left_merge_time_diff, merge_conflict_info.right_merge_time_diff
                          from merge_scenarios
                          inner join ms_metrics on ms_metrics.merge_scenario_id = merge_scenarios.id
                          inner join files on files.merge_scenarios_id = merge_scenarios.id
                          inner join chunks on chunks.file_id = files.id and chunks.has_conflict = 1
                          inner join merge_conflict_info on merge_conflict_info.chunk_id = chunks.id
                          inner join merge_conflict_metrics on merge_conflict_metrics.merge_conflict_info_id = merge_conflict_info.id;
                          """

    cursor = my_sql_connection.cursor()
    cursor.execute(sql_select_query)
    return cursor.fetchall()


# returns db.properties file to have connection with the database.
def get_properties_file():
    path = os.path.dirname(os.path.abspath(__file__))
    project_root_directory, _, _ = path.partition('/src')  # get only the root directory of the project.
    return project_root_directory + '/resources/db.properties'


# Execution starts Here #
try:
    config = configparser.ConfigParser()
    config.read(get_properties_file())
    db_username = config.get('DatabaseSection', 'database.user')
    db_password = config.get('DatabaseSection', 'database.password')
    db_name = config.get('DatabaseSection', 'database.name')
    my_sql_connection = mysql.connector.connect(host='localhost',
                                                database=db_name,
                                                user=db_username,
                                                password=db_password,
                                                auth_plugin='mysql_native_password')

    merge_scenarios_metrics_all = get_data_from_database()
    merge_scenarios_metrics_with_smaller_time = get_array_with_smaller_time_from_records(merge_scenarios_metrics_all)
    merge_scenarios_metrics = get_merged_data_per_merge_scenario(merge_scenarios_metrics_with_smaller_time)
    create_csv_file_of_merge_scenarios_metrics(merge_scenarios_metrics)

except Error as e:
    print("Error while connecting to MySQL", e)
finally:
    if my_sql_connection.is_connected():
        my_sql_connection.close()
        print("MySQL connection is closed")
