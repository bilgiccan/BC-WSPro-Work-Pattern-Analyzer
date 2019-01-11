package com.crossover.wspro

import com.crossover.sheet.GoogleSheetHelper
import com.crossover.util.ArgUtil
import com.crossover.wspro.pack.AnalyzeManager
import com.crossover.wspro.pack.XOService
import groovy.util.logging.Slf4j
import net.sourceforge.argparse4j.inf.Namespace
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@Slf4j
class WSProRunner implements CommandLineRunner {

    private static Namespace namespace

    static void main(String[] args) {
        namespace = ArgUtil.checkArgs(args)
        if(namespace == null){
            System.exit(1)
            return
        }
        SpringApplication.run(WSProRunner, args).close()
    }

    private final AnalyzeManager analyzeManager

    @Autowired
    WSProRunner(AnalyzeManager analyzeManager) {
        this.analyzeManager = analyzeManager
    }

    @Autowired
    XOService xoService

    @Override
    void run(String... args) throws Exception {
        try {
            if(ArgUtil.isAppendMode(namespace)){
                XOService.token=(xoService.postEntity(Map, [:],'identity/authentication', namespace)).token
                def dataSet = analyzeManager.bulkGemba(namespace)
                GoogleSheetHelper.write(namespace, dataSet)
            }else{
                GoogleSheetHelper.delete(namespace)
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

}
