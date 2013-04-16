Hey Kesden!
In order to make grading easier, we included a few bash scripts to do most of the work for you.

In order to run and use our framework, you need to run at least:
- 1 master
- 1 slave
- 1 client

In order to run the master server, simply execute the master.sh script: ./master.sh
In order to run the slave machine, simply execute the slave.sh script: ./slave.sh
In order to run the client (where you'll have a command prompt), simply execute the client.sh script: ./client.sh

We included three examples:
- Word Count: you'll find the config file in this folder, named wc_config.json
- Word Count 2: same as above, but without a combiner. The config file is wc_nocombiner_config.json
- Writing Level: based on the length of the words in the text files you give it, it gives you an estimated writing level. 
  The config file for that one is wl_config.json

We also included two text files: story1.txt and story2.txt, to which the config files refer to.
In order to run one of the examples above, run the client and type:
run ../wc_config.json
run ../wc_nocombiner.json
run ../wl_config.json

Then take a look inside the Results folder in this directory to see the output of the jobs.

When you run a job, you will get the ID for that job back. Then, you can do the following:
status <#ID> // will print the status of the job
stop <#ID> // will pause the job (won't send out any more tasks for it unless resumed)
start <#ID> //will unpause the job

Thanks for reading! 
