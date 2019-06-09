select field1, field2, sum(_field0)
from  table1
where field1>'2020-01-01'
and field2 = 'This is "just" a test #'
and field3 = "This   'is' \"also\" a test"
and field4 in ('a', "b", 10) # this is comments
and field5 >= 1.5
#and field5 = 1
group by field1, field2
order by field1 desc
limit 100;