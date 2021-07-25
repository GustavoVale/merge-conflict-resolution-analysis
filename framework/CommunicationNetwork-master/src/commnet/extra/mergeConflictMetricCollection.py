import mysql.connector
from mysql.connector import Error
import lizard
from guesslang import Guess
import configparser
import os

language_extension = {
    "Swift": ".swift",
    "Java": ".java",
    "Ruby": ".rb",
    "Python": ".py",
    "Objective-C": ".m",
    "Javascript": ".js",
    "C": ".c",
    "C++": ".cpp",
    "PHP": ".php",
    "C#": ".cs",
    "CSS": ".css",
    "Erlang": ".erl",
    "Go": ".go",
    "HTML": ".html",
    "Perl": ".pl",
    "Rust": ".rs",
    "SQL": ".sql",
    "Scala": ".scala",
    "Shell": ".sh",
    "Dart": ".dart",
    "Clojure": ".clj",
    "Kotlin": ".kt",
    "Groovy": ".groovy",
    "Markdown": ".md"
}  # Markdown not included


# Arguments:
# cid - merge conflict info id
# code - conflicted code
# Returns:
# complexity of code
# number of lines of code
def find_metric(cid, code):
    name = Guess().language_name(code)

    # If language can not be identified
    if name not in language_extension:
        print("-- " + name + " --")
        return None, None

    file_extension = language_extension[name]
    code_file_name = str(cid) + file_extension

    # Analyze the code
    code_metric = lizard.analyze_file.analyze_source_code(code_file_name, code)
    code_function_list_count = len(code_metric.function_list)

    overall_complexity = 0
    no_of_lines = len(code.split('\n'))

    # if function_list has more items i.e if code has multiple func/methods.
    # Loop through multiple func and get complexity for all func/methods.
    if code_function_list_count > 0:
        for i in range(0, code_function_list_count):
            complexity = code_metric.function_list[i].__dict__["cyclomatic_complexity"]
            if complexity > overall_complexity:
                overall_complexity = complexity

    else:
        overall_complexity = 1

    return overall_complexity, no_of_lines


# return all the merge conflict info ids
# These ids are use to update the table with loc and complexity by matching merge conflict info ids
def get_merge_conflict_info_id_list():
    print("get_merge_conflict_info_id_list")
    sql_select_query_mci_id = "select merge_conflict_info_id from merge_conflict_metrics"
    cursor = my_sql_connection.cursor()
    cursor.execute(sql_select_query_mci_id)
    records = cursor.fetchall()
    mci_id_list = (x[0] for x in records)
    return mci_id_list


# Updates merge conflict info metric
def update_merge_conflict_info_metric():
    print("get_merge_conflict_info")
    mci_id_list = get_merge_conflict_info_id_list()

    # loop through all the merge conflict info ids
    # Get code for merge conflict info ids and find complexity and loc for those codes
    for mci_id in mci_id_list:
        sql_select_query_code = "select left_code, right_code, merged_code from merge_conflict_info where id = " + str(mci_id)
        cursor = my_sql_connection.cursor()
        cursor.execute(sql_select_query_code)
        records = cursor.fetchall()
        for left_code, right_code, merge_code in records:
            if left_code is None:
                left_loc = 0
                left_complexity = 0
            else:
                left_complexity, left_loc = find_metric(mci_id, left_code)
            if right_code is None:
                right_loc = 0
                right_complexity = 0
            else:
                right_complexity, right_loc = find_metric(mci_id, right_code)
            if merge_code is None:
                merge_loc = 0
                merge_complexity = 0
            else:
                merge_complexity, merge_loc = find_metric(mci_id, merge_code)


            # Update merge_conflict_metrics table with following fields
            field_merge_conflict_info_metric = ("""
                update merge_conflict_metrics
                set loc = %s, left_loc = %s, right_loc = %s,
                cyclomatic_complexity = %s, left_cyclomatic_complexity = %s, right_cyclomatic_complexity = %s
                where merge_conflict_info_id = %s
                """)
            data_merge_conflict_info_metric = (merge_loc, left_loc, right_loc, merge_complexity, left_complexity, right_complexity, mci_id)
            cursor.execute(field_merge_conflict_info_metric, data_merge_conflict_info_metric)
            my_sql_connection.commit()

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

    update_merge_conflict_info_metric()

except Error as e:
    print("Error while connecting to MySQL", e)
finally:
    if my_sql_connection.is_connected():
        my_sql_connection.close()
        print("MySQL connection is closed")
