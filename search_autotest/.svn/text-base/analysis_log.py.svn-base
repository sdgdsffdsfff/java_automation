#!/usr/bin/python
#!encoding:utf-8
import sys
import re

args = sys.argv
if len(args)<2:
	print 'Please start with [LOG] file'
	exit(1)

logfile = args[1]
regpat = re.compile('pool-\d+-thread-\d+:')
part_files = {}
all_result = []
def split_file():
	global logfile, part_files
	with open(logfile, 'r') as f:
		for l in f:
			if not l.strip():
				continue
			matchs = regpat.search(l)
			if matchs:
				matchs = matchs.group()
				if matchs not in part_files:
					part_files[matchs] = []
				part_files[matchs].append(l)
			else:
				print l

def write_file():
	global part_files, all_result
	for k,v in part_files.items():
		with open(r'logs/%s.log'%k[:-1], 'w') as w:
			w.writelines(v)
		tc = []
		td = {
			'name' : 'null',
			'time' : 0,
			'pass' : 0,
			'skip' : 0,
			'excp' : 0,
			'noresult' : 0, 
			'checkfail' : 0,
			'total' : 0
		}
		rpass = rfail = rskip = i = 0
		for l in v:
			if '[ ERROR ]' in l:
				tc.extend(v[i-1:i+1])
				if 'LOG_SKIP' in l:
					td['skip'] += 1
				elif 'LOG_EXCE' in l:
					td['excp'] += 1
				elif 'NO-RESULT' in l:
					td['noresult'] += 1
				elif 'CHECK-FAIL' in l:
					td['checkfail'] += 1
			elif 'LOG_SUMMARY' in l:
				tc.append(l)
				arr = l.split('[LOG_SUMMARY] - ')[1].split(',')
				rpass, rfail, rskip = [s.split(':')[1].strip() for s in arr]
			elif 'eslipse' in l:
				name, t = l.split('eslipse for ')[1].split(':')
				td['name'] = name
				td['time'] = t
			i += 1
		with open(r'logs/%s_error.log'%td['name'], 'w') as w:
			w.writelines(tc) 
		td['pass'] = int(rpass)
		td['total'] = int(rpass) + int(rfail) + int(rskip);
		all_result.append(td)
	part_files = None
			
def report():
	global all_result
	t = '''<html xmlns="http://www.w3.org/1999/xhtml" lang="zh-cn">
		<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
		<title>搜索测试报告</title>
		<style>
		table{
            border-collapse: collapse;
            border: none;
            width: 200px;}
        td,th{
			border: solid #000 1px;}</style>
		</head>
		<body><table><tr>
		<th>模块名称</th>
		<th>&nbsp;PASS&nbsp;</th>
		<th>&nbsp;SKIP&nbsp;</th>
		<th>&nbsp;EXCP&nbsp;</th>
		<th>&nbsp;NoResult&nbsp;</th>
		<th>&nbsp;CheckFail&nbsp;</th>
		<th>&nbsp;Total&nbsp;</th>
		<th>&nbsp;Elapse(s)&nbsp;</th>
		</tr>'''
	for tr in all_result:
		t += '''<tr>
		<td>%(name)s</td>
		<td>%(pass)s</td>
		<td>%(skip)s</td>
		<td>%(excp)s</td>
		<td>%(noresult)s</td>
		<td>%(checkfail)s</td>
		<td>%(total)s</td>
		<td>%(time)s</td>
		</tr>''' % tr
	t += '''</table></body></html>'''	
	with open(r'logs/report.html', 'w') as w:
		w.writelines(t)
	return t

import smtplib
from email.mime.text import MIMEText

mailto_list=["chenxiaowu@dangdang.com"]
mail_host="sl2k8mail-01.dangdang.com"
mail_user="test_report@dangdang.com"
mail_pass="Sl-1234"
mail_port='587'
def send_mail(to_list,sub,content):
	msg = MIMEText(content, _subtype='html', _charset='utf-8')
	msg['Subject'] = sub
	msg['From'] = mail_user
	msg['To'] = ";".join(to_list)
	try:
		s=smtplib.SMTP(mail_host,mail_port)
		s.ehlo()
		s.starttls()
		s.ehlo()
		s.login(mail_user.split('@')[0],mail_pass)
		s.sendmail(mail_user, to_list, msg.as_string())
		s.close()
		return True
	except Exception, e:
		print str(e)
		return False
	
def main():
	split_file()
	write_file()
	t = report()
	if send_mail(mailto_list, u"测试报告", t):
		print u"发送成功".encode('gbk')
	else:
		print u"发送失败".encode('gbk')

if __name__=='__main__':
	main()
