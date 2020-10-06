package com.trivir.ace;


public class AceToolkitException extends Exception {
    private static final long serialVersionUID = 1L;
    private int error;
    
    public AceToolkitException(int error, String message) {
        super(message + " (" + error + ")");
        this.error = error;
    }
    
    public AceToolkitException(int error, String message, Throwable t) {
        super(message + " (" + error + ")", t);
        this.error = error;
    }

    public AceToolkitException(String message, Throwable t) {
    	super(message, t);
    	if (t.getClass().getName().equals("com.rsa.command.InvalidSessionException")){
    		this.error = API_ERROR_TIMEOUT;
    	} else if (t.getClass().getName().equals("com.rsa.session.InvalidSessionException")){
    		this.error = API_ERROR_TIMEOUT;
    	}
    	
    	this.error = API_ERROR_NOREASON;
    }
    
    public AceToolkitException(String message) {
    	super(message);
    	this.error = API_ERROR_NOREASON;
    }

    public int getError() {
        return error;
    }

    public static final int API_ERROR_BASE             = 1000;
    public static final int API_ERROR_APIDEMON_BASE    = 2000;
    /* These massages are non lookup e.i the 
       corresponding error message is not static 
       and constructed on the fly by the code.
       These messages should not be used in TAR_Error function */
    public static final int API_ERROR_APIDEMON_NLBASE  = 5000;

    /* Never change or delete old IDs */

    /* Demon errors */
    /* These errors map to the messages defined in the admin_toolkit/common/message.h */
    /* These error codes can also be produced by library itself */
    public static final int API_ERROR_AUTHINTER    = API_ERROR_BASE + 0;  /* authinter */
    public static final int API_ERROR_DAEMON       = API_ERROR_BASE + 1;  /* daemon */
    public static final int API_ERROR_FAILEDTOINIT = API_ERROR_BASE + 2;  /* failedtoinit */
    public static final int API_ERROR_FUNNOTAV     = API_ERROR_BASE + 3;  /* funnotav */
    public static final int API_ERROR_INIFORMAT    = API_ERROR_BASE + 4;  /* invalformat */
    public static final int API_ERROR_INTERFAIL    = API_ERROR_BASE + 5;  /* interfail */
    public static final int API_ERROR_LICVIOL      = API_ERROR_BASE + 6;  /* licviolat */
    public static final int API_ERROR_LONGPARAM    = API_ERROR_BASE + 7;  /* longparam */
    public static final int API_ERROR_MISSMATCH    = API_ERROR_BASE + 8;  /* missmatch */
    public static final int API_ERROR_NOCONNECT    = API_ERROR_BASE + 9;  /* noconnect */
    public static final int API_ERROR_NOREASON     = API_ERROR_BASE + 10; /* noreason */
    public static final int API_ERROR_NOTIMPLEM    = API_ERROR_BASE + 11; /* notempl */
    public static final int API_ERROR_READONLY     = API_ERROR_BASE + 12; /* readmode */
    public static final int API_ERROR_SHORTBUFFER  = API_ERROR_BASE + 13; /* shortbuffer */
    public static final int API_ERROR_SYNCFAIL     = API_ERROR_BASE + 14; /* syncfail */
    public static final int API_ERROR_TIMEOUT      = API_ERROR_BASE + 15; /* timeout */
    public static final int API_ERROR_OBSOLETE     = API_ERROR_BASE + 16; /* obsolete */
    public static final int API_ERROR_SESSION      = API_ERROR_BASE + 17; /* session */
    public static final int API_ERROR_CONNECTED    = API_ERROR_BASE + 18; /* connected */
    public static final int API_ERROR_INITENV      = API_ERROR_BASE + 19; /* initenv */

    /* DO NOT FORGET TO SET THIS CONSTANT WHEN YOU ADD BASIC SHARED MESSAGES TO ATK */
    public static final int API_MAX_MESSAGE_ARRAY  = 20;

    /* Errors related to the ATK operations */
    /* These message are produced by apidemon */
    /* Comment is the name of the old message variable for re-mapping purposes */
    public static final int API_ERROR_ACSDND                               = API_ERROR_APIDEMON_BASE + 0;     /* sesAcsDnd */
    public static final int API_ERROR_ACSADMRLTBL                          = API_ERROR_APIDEMON_BASE + 1;     /* sesAcsAdmRlTbl */
    public static final int API_ERROR_ACTMASTANDSLAVCNTBEIDNTCL            = API_ERROR_APIDEMON_BASE + 2;     /* sesActMastAndSlavCntBeIdntcl */
    public static final int API_ERROR_AGNTALREXST                          = API_ERROR_APIDEMON_BASE + 3;     /* sesAgntAlrExst */
    public static final int API_ERROR_ALCBUFF                              = API_ERROR_APIDEMON_BASE + 4;     /* sesAlcBuff */
    public static final int API_ERROR_ALCRESBUF                            = API_ERROR_APIDEMON_BASE + 5;     /* sesAlcResBuf */
    public static final int API_ERROR_ALPHANTALWD                          = API_ERROR_APIDEMON_BASE + 6;     /* sesAlphaNtAlwd */
    public static final int API_ERROR_ATTISNTCONF                          = API_ERROR_APIDEMON_BASE + 7;     /* sesAttIsNtConf */
    public static final int API_ERROR_ATTNTFND                             = API_ERROR_APIDEMON_BASE + 8;     /* sesAttNtFnd */
    public static final int API_ERROR_ATTVALEXST                           = API_ERROR_APIDEMON_BASE + 9;     /* sesAttValExst */
    public static final int API_ERROR_ATTVALNTFND                          = API_ERROR_APIDEMON_BASE + 10;    /* sesAttValNtFnd */
    public static final int API_ERROR_AUTHNOTINIT                          = API_ERROR_APIDEMON_BASE + 11;    /* sesAuthNotInit */
    public static final int API_ERROR_AUTHREQ                              = API_ERROR_APIDEMON_BASE + 12;    /* sesAuthReq */
    public static final int API_ERROR_CLSLOG                               = API_ERROR_APIDEMON_BASE + 13;    /* sesClsLog */
    public static final int API_ERROR_CNTASGNEXPTKN                        = API_ERROR_APIDEMON_BASE + 14;    /* sesCntAsgnExpTkn */
    public static final int API_ERROR_CNTCRTSOFTDBFL                       = API_ERROR_APIDEMON_BASE + 15;    /* sesCntCrtSoftDBFl */
    public static final int API_ERROR_CNTDELAGNTWEXT                       = API_ERROR_APIDEMON_BASE + 16;    /* sesCntDelAgntWExt */
    public static final int API_ERROR_CNTDELAGNTWGRPS                      = API_ERROR_APIDEMON_BASE + 17;    /* sesCntDelAgntWGrps */
    public static final int API_ERROR_CNTDELAGNTWSECNODE                   = API_ERROR_APIDEMON_BASE + 18;    /* sesCntDelAgntWSecNode */
    public static final int API_ERROR_CNTDELAGNTWUSR                       = API_ERROR_APIDEMON_BASE + 19;    /* sesCntDelAgntWUsr */
    public static final int API_ERROR_CNTDELGRPENBONAGNT                   = API_ERROR_APIDEMON_BASE + 20;    /* sesCntDelGrpEnbOnAgnt */
    public static final int API_ERROR_CNTDELGRPWEXT                        = API_ERROR_APIDEMON_BASE + 21;    /* sesCntDelGrpWExt */
    public static final int API_ERROR_CNTDELGRPWGRPADM                     = API_ERROR_APIDEMON_BASE + 22;    /* sesCntDelGrpWGrpAdm */
    public static final int API_ERROR_CNTDELGRPWUSR                        = API_ERROR_APIDEMON_BASE + 23;    /* sesCntDelGrpWUsr */
    public static final int API_ERROR_CNTDELSITWAGNT                       = API_ERROR_APIDEMON_BASE + 24;    /* sesCntDelSitWAgnt */
    public static final int API_ERROR_CNTDELSITWEXT                        = API_ERROR_APIDEMON_BASE + 25;    /* sesCntDelSitWExt */
    public static final int API_ERROR_CNTDELSITWGRP                        = API_ERROR_APIDEMON_BASE + 26;    /* sescntDelSitWGrp */
    public static final int API_ERROR_CNTDELSITWSITADMN                    = API_ERROR_APIDEMON_BASE + 27;    /* sesCntDelSitWSitAdmn */
    public static final int API_ERROR_CNTDELTKWEXT                         = API_ERROR_APIDEMON_BASE + 28;    /* sesCntDelTkWExt */
    public static final int API_ERROR_CNTDELUSRENBONAGNT                   = API_ERROR_APIDEMON_BASE + 29;    /* sesCntDelUsrEnbOnAgnt */
    public static final int API_ERROR_CNTDELUSRINGRP                       = API_ERROR_APIDEMON_BASE + 30;    /* sesCntDelUsrInGrp */
    public static final int API_ERROR_CNTDELUSRWEXT                        = API_ERROR_APIDEMON_BASE + 31;    /* sesCntDelUsrWExt */
    public static final int API_ERROR_CNTFCHATT                            = API_ERROR_APIDEMON_BASE + 32;    /* sesCntFchAtt */
    public static final int API_ERROR_CNTFCHATTVAL                         = API_ERROR_APIDEMON_BASE + 33;    /* sesCntFchAttVal */
    public static final int API_ERROR_CNTFNDPREDVAL                        = API_ERROR_APIDEMON_BASE + 34;    /* sesCntFndPredVal */
    public static final int API_ERROR_CNTINSATTVAL                         = API_ERROR_APIDEMON_BASE + 35;    /* sesCntInsAttVal */
    public static final int API_ERROR_CNTLCKUSR                            = API_ERROR_APIDEMON_BASE + 36;    /* sesCntLckUsr */
    public static final int API_ERROR_CNTOPNATTTBL                         = API_ERROR_APIDEMON_BASE + 37;    /* sesCntOpnAttTbl */
    public static final int API_ERROR_CNTOPNATTVALTBL                      = API_ERROR_APIDEMON_BASE + 38;    /* sesCntOpnAttValTbl */
    public static final int API_ERROR_CNTOPNLOG                            = API_ERROR_APIDEMON_BASE + 39;    /* sesCntOpnLog */
    public static final int API_ERROR_CNTUPDATTVAL                         = API_ERROR_APIDEMON_BASE + 40;    /* sesCntUpdAttVal */
    public static final int API_ERROR_CNTUPDPRF                            = API_ERROR_APIDEMON_BASE + 41;    /* sesCntUpdPrf */
    public static final int API_ERROR_CNTUPDUSRREC                         = API_ERROR_APIDEMON_BASE + 42;    /* sesCntUpdUsrRec */
    public static final int API_ERROR_CNVTKNDATA                           = API_ERROR_APIDEMON_BASE + 43;    /* sesCnvTknData */
    public static final int API_ERROR_DCRYAGNTSECBLK                       = API_ERROR_APIDEMON_BASE + 44;    /* sesDcryAgntSecBlk */
    public static final int API_ERROR_DCRYTKNSECBLK                        = API_ERROR_APIDEMON_BASE + 45;    /* sesDcryTknSecBlk */
    public static final int API_ERROR_DECSYSREC                            = API_ERROR_APIDEMON_BASE + 46;    /* sesDecSysRec */
    public static final int API_ERROR_DELLOGENTRY                          = API_ERROR_APIDEMON_BASE + 47;    /* sesDelLogEntry */
    public static final int API_ERROR_DELPRF                               = API_ERROR_APIDEMON_BASE + 48;    /* sesDelPrf */
    public static final int API_ERROR_DMPFLD                               = API_ERROR_APIDEMON_BASE + 49;    /* sesDmpFld */
    public static final int API_ERROR_EMPKEYNTALWD                         = API_ERROR_APIDEMON_BASE + 50;    /* sesEmpKeyNtAlwd */
    public static final int API_ERROR_ENCRAGTSECBLK                        = API_ERROR_APIDEMON_BASE + 51;    /* sesEncrAgtSecBlk */
    public static final int API_ERROR_ENCROTPSECBLK                        = API_ERROR_APIDEMON_BASE + 52;    /* sesEncrOTPSecBlk */
    public static final int API_ERROR_ENCRTKNSECBLK                        = API_ERROR_APIDEMON_BASE + 53;    /* sesEncrTknSecBlk */
    public static final int API_ERROR_EXPDTISPAST                          = API_ERROR_APIDEMON_BASE + 54;    /* sesExpDtIsPast */
    public static final int API_ERROR_FCHADMRLTBL                          = API_ERROR_APIDEMON_BASE + 55;    /* sesFchAdmRlTbl */
    public static final int API_ERROR_FCHAGNTEXTTBL                        = API_ERROR_APIDEMON_BASE + 56;    /* sesFchAgntExtTbl */
    public static final int API_ERROR_FCHAGNTTBL                           = API_ERROR_APIDEMON_BASE + 57;    /* sesFchAgntTbl */
    public static final int API_ERROR_FCHENBGRPTBL                         = API_ERROR_APIDEMON_BASE + 58;    /* sesFchEnbGrpTbl */
    public static final int API_ERROR_FCHENBUSRTBL                         = API_ERROR_APIDEMON_BASE + 59;    /* sesFchEnbUsrTbl */
    public static final int API_ERROR_FCHGRPEXTTBL                         = API_ERROR_APIDEMON_BASE + 60;    /* sesFchGrpExtTbl */
    public static final int API_ERROR_FCHGRPMBRTBL                         = API_ERROR_APIDEMON_BASE + 61;    /* sesFchGrpMbrTbl */
    public static final int API_ERROR_FCHGRPTBL                            = API_ERROR_APIDEMON_BASE + 62;    /* sesFchGrpTbl */
    public static final int API_ERROR_FCHNODTBL                            = API_ERROR_APIDEMON_BASE + 63;    /* sesFchNodTbl */
    public static final int API_ERROR_FCHPRF                               = API_ERROR_APIDEMON_BASE + 64;    /* sesFchPrf */
    public static final int API_ERROR_FCHREPLTBL                           = API_ERROR_APIDEMON_BASE + 65;    /* sesFchReplTbl */
    public static final int API_ERROR_FCHSITEXTTBL                         = API_ERROR_APIDEMON_BASE + 66;    /* sesFchSitExtTbl */
    public static final int API_ERROR_FCHSITTBL                            = API_ERROR_APIDEMON_BASE + 67;    /* sesFchSitTbl */
    public static final int API_ERROR_FCHTKNEXTTBL                         = API_ERROR_APIDEMON_BASE + 68;    /* sesFchTknExtTbl */
    public static final int API_ERROR_FCHTKNREC                            = API_ERROR_APIDEMON_BASE + 69;    /* sesFchTknRec */
    public static final int API_ERROR_FCHTKNTBL                            = API_ERROR_APIDEMON_BASE + 70;    /* sesFchTknTbl */
    public static final int API_ERROR_FCHTSKITM                            = API_ERROR_APIDEMON_BASE + 71;    /* sesFchTskItm */
    public static final int API_ERROR_FCHTSKLST                            = API_ERROR_APIDEMON_BASE + 72;    /* sesFchTskLst */
    public static final int API_ERROR_FCHUSREXTTBL                         = API_ERROR_APIDEMON_BASE + 73;    /* sesFchUsrExtTbl */
    public static final int API_ERROR_FCHUSRTBL                            = API_ERROR_APIDEMON_BASE + 74;    /* sesFchUsrTbl */
    public static final int API_ERROR_FILECRP                              = API_ERROR_APIDEMON_BASE + 75;    /* sesFileCrp */
    public static final int API_ERROR_FLDCHKLCMP                           = API_ERROR_APIDEMON_BASE + 76;    /* sesFldChkLCmp */
    public static final int API_ERROR_FLDCNTTKN                            = API_ERROR_APIDEMON_BASE + 77;    /* sesFldCntTkn */
    public static final int API_ERROR_FLDDBLGN                             = API_ERROR_APIDEMON_BASE + 78;    /* sesFldDBLgn */
    public static final int API_ERROR_FLDDELPASS                           = API_ERROR_APIDEMON_BASE + 79;    /* sesFldDelPass */
    public static final int API_ERROR_FLDDELTKNREC                         = API_ERROR_APIDEMON_BASE + 80;    /* sesFldDelTknRec */
    public static final int API_ERROR_FLDDELUSR                            = API_ERROR_APIDEMON_BASE + 81;    /* sesFldDelUsr */
    public static final int API_ERROR_FLDFCHLOGENTRY                       = API_ERROR_APIDEMON_BASE + 82;    /* sesFldFchLogEntry */
    public static final int API_ERROR_FLDGENOTP                            = API_ERROR_APIDEMON_BASE + 83;    /* sesFldGenOTP */
    public static final int API_ERROR_FLDGENOTPCODE                        = API_ERROR_APIDEMON_BASE + 84;    /* sesFldGenOTPCode */
    public static final int API_ERROR_FLDGENPSWDTKNSRLNUM                  = API_ERROR_APIDEMON_BASE + 85;    /* sesFldGenPswdTknSrlNum */
    public static final int API_ERROR_FLDGTGRPINFO                         = API_ERROR_APIDEMON_BASE + 86;    /* sesFldGtGrpInfo */
    public static final int API_ERROR_FLDGTSEQNUM                          = API_ERROR_APIDEMON_BASE + 87;    /* sesFldGtSeqNum */
    public static final int API_ERROR_FLDGTSITINFO                         = API_ERROR_APIDEMON_BASE + 88;    /* sesFldGtSitInfo */
    public static final int API_ERROR_FLDGTSYSREC                          = API_ERROR_APIDEMON_BASE + 89;    /* sesFldGtSysRec */
    public static final int API_ERROR_FLDGTTKNREC                          = API_ERROR_APIDEMON_BASE + 90;    /* sesFldGtTknRec */
    public static final int API_ERROR_FLDGTTSKLST                          = API_ERROR_APIDEMON_BASE + 91;    /* sesFldGtTskLst */
    public static final int API_ERROR_FLDLCKTKNREC                         = API_ERROR_APIDEMON_BASE + 92;    /* sesFldLckTknRec */
    public static final int API_ERROR_FLDLCKUSREXTREC                      = API_ERROR_APIDEMON_BASE + 93;    /* sesFldLckUsrExtRec */
    public static final int API_ERROR_FLDSTPNTONXTTKNCOD                   = API_ERROR_APIDEMON_BASE + 94;    /* sesFldStPnToNxtTknCod */
    public static final int API_ERROR_FLDSTSDIPRCSNM                       = API_ERROR_APIDEMON_BASE + 95;    /* sesFldStSDIPrcsNm */
    public static final int API_ERROR_FLDSVSOFTPSWDINFL                    = API_ERROR_APIDEMON_BASE + 96;    /* sesFldSvSoftPswdInFl */
    public static final int API_ERROR_FLDTKNCNT                            = API_ERROR_APIDEMON_BASE + 97;    /* sesFldTknCnt */
    public static final int API_ERROR_FLDTKNLCK                            = API_ERROR_APIDEMON_BASE + 98;    /* sesFldTknLck */
    public static final int API_ERROR_FLDUPDTKN                            = API_ERROR_APIDEMON_BASE + 99;    /* sesFldUpdTkn */
    public static final int API_ERROR_FLNMORTRNCOPTSPEC                    = API_ERROR_APIDEMON_BASE + 100;   /* sesFlNmORTrncOptSpec */
    public static final int API_ERROR_FNCDSBLWACMTTRUE                     = API_ERROR_APIDEMON_BASE + 101;   /* sesFncDsblWAcmtTrue */
    public static final int API_ERROR_GRPALRENBAGNT                        = API_ERROR_APIDEMON_BASE + 102;   /* sesGrpAlrEnbAgnt */
    public static final int API_ERROR_GRPALREXST                           = API_ERROR_APIDEMON_BASE + 103;   /* sesGrpAlrExst */
    public static final int API_ERROR_GRPALREXSTINDB                       = API_ERROR_APIDEMON_BASE + 104;   /* sesGrpAlrExstInDB */
    public static final int API_ERROR_GTAGNT                               = API_ERROR_APIDEMON_BASE + 105;   /* sesGtAgnt */
    public static final int API_ERROR_GTAGNTREC                            = API_ERROR_APIDEMON_BASE + 106;   /* sesGtAgntRec */
    public static final int API_ERROR_GTAGNTTYP                            = API_ERROR_APIDEMON_BASE + 107;   /* sesGtAgntTyp */
    public static final int API_ERROR_GTGRP                                = API_ERROR_APIDEMON_BASE + 108;   /* sesGtGrp */
    public static final int API_ERROR_GTLCKTKNPAIR                         = API_ERROR_APIDEMON_BASE + 109;   /* sesGtLckTknPair */
    public static final int API_ERROR_GTLOGENTY                            = API_ERROR_APIDEMON_BASE + 110;   /* sesGtLogEnty */
    public static final int API_ERROR_GTRLM                                = API_ERROR_APIDEMON_BASE + 111;   /* sesGtRlm */
    public static final int API_ERROR_GTSIT                                = API_ERROR_APIDEMON_BASE + 112;   /* sesGtSit */
    public static final int API_ERROR_GTUSRINFO                            = API_ERROR_APIDEMON_BASE + 113;   /* sesGtUsrInfo */
    public static final int API_ERROR_HSTNFND                              = API_ERROR_APIDEMON_BASE + 114;   /* sesHstNFnd */
    public static final int API_ERROR_INSGRPREC                            = API_ERROR_APIDEMON_BASE + 115;   /* sesInsGrpRec */
    public static final int API_ERROR_INSPRF                               = API_ERROR_APIDEMON_BASE + 116;   /* sesInsPrf */
    public static final int API_ERROR_INSTKNREC                            = API_ERROR_APIDEMON_BASE + 117;   /* sesInsTknRec */
    public static final int API_ERROR_INTDBERR                             = API_ERROR_APIDEMON_BASE + 118;   /* sesIntDBErr */
    public static final int API_ERROR_INVACTMAST                           = API_ERROR_APIDEMON_BASE + 119;   /* sesInvActMast */
    public static final int API_ERROR_INVAGNT                              = API_ERROR_APIDEMON_BASE + 120;   /* sesInvAgnt */
    public static final int API_ERROR_INVAGNTOADD                          = API_ERROR_APIDEMON_BASE + 121;   /* sesInvAgntOAdd */
    public static final int API_ERROR_INVAGNTTYP                           = API_ERROR_APIDEMON_BASE + 122;   /* sesInvAgntTyp */
    public static final int API_ERROR_INVARG                               = API_ERROR_APIDEMON_BASE + 123;   /* sesInvArg */
    public static final int API_ERROR_INVATT                               = API_ERROR_APIDEMON_BASE + 124;   /* sesInvAtt */
    public static final int API_ERROR_INVATTVAL                            = API_ERROR_APIDEMON_BASE + 125;   /* sesInvAttVal */
    public static final int API_ERROR_INVENCRTYP                           = API_ERROR_APIDEMON_BASE + 126;   /* sesInvEncrTyp */
    public static final int API_ERROR_INVENDDT                             = API_ERROR_APIDEMON_BASE + 127;   /* sesInvEndDt */
    public static final int API_ERROR_INVFLGCMB                            = API_ERROR_APIDEMON_BASE + 128;   /* sesInvFlgCmb */
    public static final int API_ERROR_INVFLGVAL                            = API_ERROR_APIDEMON_BASE + 129;   /* sesInvFlgVal */
    public static final int API_ERROR_INVFRSTNM                            = API_ERROR_APIDEMON_BASE + 130;   /* sesInvFrstNm */
    public static final int API_ERROR_INVGRP                               = API_ERROR_APIDEMON_BASE + 131;   /* sesInvGrp */
    public static final int API_ERROR_INVGRPLGN                            = API_ERROR_APIDEMON_BASE + 132;   /* sesInvGrpLgn */
    public static final int API_ERROR_INVHRVAL                             = API_ERROR_APIDEMON_BASE + 133;   /* sesInvHrVal */
    public static final int API_ERROR_INVKEY                               = API_ERROR_APIDEMON_BASE + 134;   /* sesInvKey */
    public static final int API_ERROR_INVKEYFLGVAL                         = API_ERROR_APIDEMON_BASE + 135;   /* sesInvKeyFlgVal */
    public static final int API_ERROR_INVLGN                               = API_ERROR_APIDEMON_BASE + 136;   /* sesInvLgn */
    public static final int API_ERROR_INVLGNLSTNM                          = API_ERROR_APIDEMON_BASE + 137;   /* sesInvLgnLstNm */
    public static final int API_ERROR_INVLIFEVAL                           = API_ERROR_APIDEMON_BASE + 138;   /* sesInvLifeVal */
    public static final int API_ERROR_INVLSTNM                             = API_ERROR_APIDEMON_BASE + 139;   /* sesInvLstNm */
    public static final int API_ERROR_INVMETFLGVAL                         = API_ERROR_APIDEMON_BASE + 140;   /* sesInvMetFlgVal */
    public static final int API_ERROR_INVMODE                              = API_ERROR_APIDEMON_BASE + 141;   /* sesInvMode */
    public static final int API_ERROR_INVNUMDAYS                           = API_ERROR_APIDEMON_BASE + 142;   /* sesInvNumDays */
    public static final int API_ERROR_INVOPT                               = API_ERROR_APIDEMON_BASE + 143;   /* sesInvOpt */
    public static final int API_ERROR_INVPN                                = API_ERROR_APIDEMON_BASE + 144;   /* sesInvPn */
    public static final int API_ERROR_INVPRF                               = API_ERROR_APIDEMON_BASE + 145;   /* sesInvPrf */
    public static final int API_ERROR_INVPROTFLGVAL                        = API_ERROR_APIDEMON_BASE + 146;   /* sesInvProtFlgVal */
    public static final int API_ERROR_INVPSWD                              = API_ERROR_APIDEMON_BASE + 147;   /* sesInvPswd */
    public static final int API_ERROR_INVPSWDTKNOPT                        = API_ERROR_APIDEMON_BASE + 148;   /* sesInvPswdTknOpt */
    public static final int API_ERROR_INVRLM                               = API_ERROR_APIDEMON_BASE + 149;   /* sesInvRlm */
    public static final int API_ERROR_INVRLMNM                             = API_ERROR_APIDEMON_BASE + 150;   /* sesInvRlmNm */
    public static final int API_ERROR_INVRMTALIS                           = API_ERROR_APIDEMON_BASE + 151;   /* sesInvRmtAlis */
    public static final int API_ERROR_INVSCRT                              = API_ERROR_APIDEMON_BASE + 152;   /* sesInvScrt */
    public static final int API_ERROR_INVSHL                               = API_ERROR_APIDEMON_BASE + 153;   /* sesInvShl */
    public static final int API_ERROR_INVSIT                               = API_ERROR_APIDEMON_BASE + 154;   /* sesInvSit */
    public static final int API_ERROR_INVSORTMODE                          = API_ERROR_APIDEMON_BASE + 155;   /* sesInvSortMode */
    public static final int API_ERROR_INVSTRTDT                            = API_ERROR_APIDEMON_BASE + 156;   /* sesInvStrtDt */
    public static final int API_ERROR_INVTKN                               = API_ERROR_APIDEMON_BASE + 157;   /* sesInvTkn */
    public static final int API_ERROR_INVTKNCOD                            = API_ERROR_APIDEMON_BASE + 158;   /* sesInvTknCod */
    public static final int API_ERROR_INVTKNRNGE                           = API_ERROR_APIDEMON_BASE + 159;   /* sesInvTknRnge */
    public static final int API_ERROR_INVTKNSRLEND                         = API_ERROR_APIDEMON_BASE + 160;   /* sesInvTknSrlEnd */
    public static final int API_ERROR_INVTKNSRLSTRT                        = API_ERROR_APIDEMON_BASE + 161;   /* sesInvTknSrlStrt */
    public static final int API_ERROR_INVTSKLST                            = API_ERROR_APIDEMON_BASE + 162;   /* sesInvTskLst */
    public static final int API_ERROR_INVUSR                               = API_ERROR_APIDEMON_BASE + 163;   /* sesInvUsr */
    public static final int API_ERROR_IPALREXSTINDBASSECNOD                = API_ERROR_APIDEMON_BASE + 164;   /* sesIPAlrExstInDBAsSecNod */
    public static final int API_ERROR_KEYNTUNQ                             = API_ERROR_APIDEMON_BASE + 165;   /* sesKeyNtUnq */
    public static final int API_ERROR_KEYREQVALPASS                        = API_ERROR_APIDEMON_BASE + 166;   /* sesKeyReqValPass */
    public static final int API_ERROR_LCKATTVAL                            = API_ERROR_APIDEMON_BASE + 167;   /* sesLckAttVal */
    public static final int API_ERROR_LGNALREXSTAGNT                       = API_ERROR_APIDEMON_BASE + 168;   /* sesLgnAlrExstAgnt */
    public static final int API_ERROR_LGNALREXSTINGRP                      = API_ERROR_APIDEMON_BASE + 169;   /* sesLgnAlrExstInGrp */
    public static final int API_ERROR_MANCMTMDNTSUPP                       = API_ERROR_APIDEMON_BASE + 170;   /* sesManCmtMdNtSupp */
    public static final int API_ERROR_MAXTKNALRASGN                        = API_ERROR_APIDEMON_BASE + 171;   /* sesMaxTknAlrAsgn */
    public static final int API_ERROR_NAMELOCKFAILED                       = API_ERROR_APIDEMON_BASE + 172;   /* sesNameLockFailed */
    public static final int API_ERROR_NWPNREJACSDND                        = API_ERROR_APIDEMON_BASE + 173;   /* sesNwPnRejAcsDnd */
    public static final int API_ERROR_NOOTPAVL                             = API_ERROR_APIDEMON_BASE + 174;   /* sesNoOTPAvl */
    public static final int API_ERROR_OLDTKNISINRPLPR                      = API_ERROR_APIDEMON_BASE + 175;   /* sesOldTknIsInRplPr */
    public static final int API_ERROR_OPNADMRLTBL                          = API_ERROR_APIDEMON_BASE + 176;   /* sesOpnAdmRlTbl */
    public static final int API_ERROR_OPNAGNTEXTTBL                        = API_ERROR_APIDEMON_BASE + 177;   /* sesOpnAgntExtTbl */
    public static final int API_ERROR_OPNAGNTTBL                           = API_ERROR_APIDEMON_BASE + 178;   /* sesOpnAgntTbl */
    public static final int API_ERROR_OPNCACHFLE                           = API_ERROR_APIDEMON_BASE + 179;   /* sesOpnCachFle */
    public static final int API_ERROR_OPNENBGRPTBL                         = API_ERROR_APIDEMON_BASE + 180;   /* sesOpnEnbGrpTbl */
    public static final int API_ERROR_OPNENBUSRTBL                         = API_ERROR_APIDEMON_BASE + 181;   /* sesOpnEnbUsrTbl */
    public static final int API_ERROR_OPNFILE                              = API_ERROR_APIDEMON_BASE + 182;   /* sesOpnFile */
    public static final int API_ERROR_OPNGRPEXTTBL                         = API_ERROR_APIDEMON_BASE + 183;   /* sesOpnGrpExtTbl */
    public static final int API_ERROR_OPNGRPMBRTBL                         = API_ERROR_APIDEMON_BASE + 184;   /* sesOpnGrpMbrTbl */
    public static final int API_ERROR_OPNGRPTBL                            = API_ERROR_APIDEMON_BASE + 185;   /* sesOpnGrpTbl */
    public static final int API_ERROR_OPNIMPTFILE                          = API_ERROR_APIDEMON_BASE + 186;   /* sesOpnImptFile */
    public static final int API_ERROR_OPNNODTBL                            = API_ERROR_APIDEMON_BASE + 187;   /* sesOpnNodTbl */
    public static final int API_ERROR_OPNPRFTBL                            = API_ERROR_APIDEMON_BASE + 188;   /* sesOpnPrfTbl */
    public static final int API_ERROR_OPNREPFILE                           = API_ERROR_APIDEMON_BASE + 189;   /* sesOpnRepFile */
    public static final int API_ERROR_OPNREPLTBL                           = API_ERROR_APIDEMON_BASE + 190;   /* sesOpnReplTbl */
    public static final int API_ERROR_OPNSITEXTTBL                         = API_ERROR_APIDEMON_BASE + 191;   /* sesOpnSitExtTbl */
    public static final int API_ERROR_OPNSITTBL                            = API_ERROR_APIDEMON_BASE + 192;   /* sesOpnSitTbl */
    public static final int API_ERROR_OPNTKNEXTTBL                         = API_ERROR_APIDEMON_BASE + 193;   /* sesOpnTknExtTbl */
    public static final int API_ERROR_OPNTKNTBL                            = API_ERROR_APIDEMON_BASE + 194;   /* sesOpnTknTbl */
    public static final int API_ERROR_OPNTSKITMCURS                        = API_ERROR_APIDEMON_BASE + 195;   /* sesOpnTskItmCurs */
    public static final int API_ERROR_OPNTSKLSTCURS                        = API_ERROR_APIDEMON_BASE + 196;   /* sesOpnTskLstCurs */
    public static final int API_ERROR_OPNUSREXTTBL                         = API_ERROR_APIDEMON_BASE + 197;   /* sesOpnUsrExtTbl */
    public static final int API_ERROR_OPNUSRTBL                            = API_ERROR_APIDEMON_BASE + 198;   /* sesOpnUsrTbl */
    public static final int API_ERROR_PNPSWDTKNCNTBECLRD                   = API_ERROR_APIDEMON_BASE + 199;   /* sesPnPswdTknCntBeClrd */
    public static final int API_ERROR_PREPASCIISESS                        = API_ERROR_APIDEMON_BASE + 200;   /* sesPrepASCIISess */
    public static final int API_ERROR_PRFEXST                              = API_ERROR_APIDEMON_BASE + 201;   /* sesPrfExst */
    public static final int API_ERROR_PRFISASGNUSR                         = API_ERROR_APIDEMON_BASE + 202;   /* sesPrfIsAsgnUsr */
    public static final int API_ERROR_PRFNTFND                             = API_ERROR_APIDEMON_BASE + 203;   /* sesPrfNtFnd */
    public static final int API_ERROR_PRNTFND                              = API_ERROR_APIDEMON_BASE + 204;   /* sesPrNtFnd */
    public static final int API_ERROR_PSWDALREXST                          = API_ERROR_APIDEMON_BASE + 205;   /* sesPswdAlrExst */
    public static final int API_ERROR_PSWDTKNCNTBELST                      = API_ERROR_APIDEMON_BASE + 206;   /* sesPswdTknCntBeLst */
    public static final int API_ERROR_PSWDTKNCNTBEREPL                     = API_ERROR_APIDEMON_BASE + 207;   /* sesPswdTknCntBeRepl */
    public static final int API_ERROR_RECLLOG                              = API_ERROR_APIDEMON_BASE + 208;   /* sesReclLog */
    public static final int API_ERROR_REOPNLOG                             = API_ERROR_APIDEMON_BASE + 209;   /* sesReopnLog */
    public static final int API_ERROR_RPLTKNCNTEXPORT                      = API_ERROR_APIDEMON_BASE + 210;   /* sesRplTknCntExport */
    public static final int API_ERROR_SELTKN                               = API_ERROR_APIDEMON_BASE + 211;   /* sesSelTkn */
    public static final int API_ERROR_SITALREXSTINDB                       = API_ERROR_APIDEMON_BASE + 212;   /* sesSitAlrExstInDB */
    public static final int API_ERROR_STRTDTTMISLTRTHNENDDTANDTM           = API_ERROR_APIDEMON_BASE + 213;   /* sesStrtDtTmIsLtrThnEndDtAndTm */
    public static final int API_ERROR_SYSPNNTALWD                          = API_ERROR_APIDEMON_BASE + 214;   /* sesSysPnNtAlwd */
    public static final int API_ERROR_TKNALRASGN                           = API_ERROR_APIDEMON_BASE + 215;   /* sesTknAlrAsgn */
    public static final int API_ERROR_TKNALRDEPL                           = API_ERROR_APIDEMON_BASE + 216;   /* sesTknAlrDepl */
    public static final int API_ERROR_TKNALREXST                           = API_ERROR_APIDEMON_BASE + 217;   /* sesTknAlrExst */
    public static final int API_ERROR_TKNDEPLLIMRCHD                       = API_ERROR_APIDEMON_BASE + 218;   /* sesTknDeplLimRchd */
    public static final int API_ERROR_TKNISINRPLPR                         = API_ERROR_APIDEMON_BASE + 219;   /* sesTknIsInRplPr */
    public static final int API_ERROR_TKNNTASGN                            = API_ERROR_APIDEMON_BASE + 220;   /* sesTknNtAsgn */
    public static final int API_ERROR_TKNNTLST                             = API_ERROR_APIDEMON_BASE + 221;   /* sesTknNtLst */
    public static final int API_ERROR_TKNNTSOFTTYP                         = API_ERROR_APIDEMON_BASE + 222;   /* sesTknNtSoftTyp */
    public static final int API_ERROR_UNBACSAGNTDLL                        = API_ERROR_APIDEMON_BASE + 223;   /* sesUnbAcsAgntDLL */
    public static final int API_ERROR_UNBADDOTPSET                         = API_ERROR_APIDEMON_BASE + 224;   /* sesUnbAddOTPSet */
    public static final int API_ERROR_UNBADDRECTOSOFTDB                    = API_ERROR_APIDEMON_BASE + 225;   /* sesUnbAddRecToSoftDB */
    public static final int API_ERROR_UNBCNFGREC                           = API_ERROR_APIDEMON_BASE + 226;   /* sesUnbCnfgRec */
    public static final int API_ERROR_UNBCNTOTP                            = API_ERROR_APIDEMON_BASE + 227;   /* sesUnbCntOTP */
    public static final int API_ERROR_UNBCOMMAUTHSRV                       = API_ERROR_APIDEMON_BASE + 228;   /* sesUnbCommAuthSrv */
    public static final int API_ERROR_UNBDELADMREC                         = API_ERROR_APIDEMON_BASE + 229;   /* sesUnbDelAdmRec */
    public static final int API_ERROR_UNBDELADMRLREC                       = API_ERROR_APIDEMON_BASE + 230;   /* sesUnbDelAdmRlRec */
    public static final int API_ERROR_UNBDELAGNT                           = API_ERROR_APIDEMON_BASE + 231;   /* sesUnbDelAgnt */
    public static final int API_ERROR_UNBDELATTVAL                         = API_ERROR_APIDEMON_BASE + 232;   /* sesUnbDelAttVal */
    public static final int API_ERROR_UNBDELENBGRPREC                      = API_ERROR_APIDEMON_BASE + 233;   /* sesUnbDelEnbGrpRec */
    public static final int API_ERROR_UNBDELENBUSRREC                      = API_ERROR_APIDEMON_BASE + 234;   /* sesUnbDelEnbUsrRec */
    public static final int API_ERROR_UNBDELGRP                            = API_ERROR_APIDEMON_BASE + 235;   /* sesUnbDelGrp */
    public static final int API_ERROR_UNBDELGRPMBRREC                      = API_ERROR_APIDEMON_BASE + 236;   /* sesUnbDelGrpMbrRec */
    public static final int API_ERROR_UNBDELOTPS                           = API_ERROR_APIDEMON_BASE + 237;   /* sesUnbDelOTPs */
    public static final int API_ERROR_UNBDELSIT                            = API_ERROR_APIDEMON_BASE + 238;   /* sesUnbDelSit */
    public static final int API_ERROR_UNBDELTKNREC                         = API_ERROR_APIDEMON_BASE + 239;   /* sesUnbDelTknRec */
    public static final int API_ERROR_UNBDELUSREXTREC                      = API_ERROR_APIDEMON_BASE + 240;   /* sesUnbDelUsrExtRec */
    public static final int API_ERROR_UNBDETCURHST                         = API_ERROR_APIDEMON_BASE + 241;   /* sesUnbDetCurHst */
    public static final int API_ERROR_UNBDETPRIREPLSTAT                    = API_ERROR_APIDEMON_BASE + 242;   /* sesUnbDetPriReplStat */
    public static final int API_ERROR_UNBENCRSOFTSEED                      = API_ERROR_APIDEMON_BASE + 243;   /* sesUnbEncrSoftSeed */
    public static final int API_ERROR_UNBGTENBUSRREC                       = API_ERROR_APIDEMON_BASE + 244;   /* sesUnbGtEnbUsrRec */
    public static final int API_ERROR_UNBGTGRPMBRREC                       = API_ERROR_APIDEMON_BASE + 245;   /* sesUnbGtGrpMbrRec */
    public static final int API_ERROR_UNBGTPINPARAM                        = API_ERROR_APIDEMON_BASE + 246;   /* sesUnbGtPinParam */
    public static final int API_ERROR_UNBGTREPLBYNM                        = API_ERROR_APIDEMON_BASE + 247;   /* sesUnbGtReplByNm */
    public static final int API_ERROR_UNBGTSYSREC                          = API_ERROR_APIDEMON_BASE + 248;   /* sesUnbGtSysRec */
    public static final int API_ERROR_UNBGTTKNUSR                          = API_ERROR_APIDEMON_BASE + 249;   /* sesUnbGtTknUsr */
    public static final int API_ERROR_UNBGTUSRBYTKN                        = API_ERROR_APIDEMON_BASE + 250;   /* sesUnbGtUsrByTkn */
    public static final int API_ERROR_UNBGTUSRREC                          = API_ERROR_APIDEMON_BASE + 251;   /* sesUnbGtUsrRec */
    public static final int API_ERROR_UNBINSAGNT                           = API_ERROR_APIDEMON_BASE + 252;   /* sesUnbInsAgnt */
    public static final int API_ERROR_UNBINSENBGRPREC                      = API_ERROR_APIDEMON_BASE + 253;   /* sesUnbInsEnbGrpRec */
    public static final int API_ERROR_UNBINSENBUSR                         = API_ERROR_APIDEMON_BASE + 254;   /* sesUnbInsEnbUsr */
    public static final int API_ERROR_UNBINSUSRREC                         = API_ERROR_APIDEMON_BASE + 255;   /* sesUnbInsUsrRec */
    public static final int API_ERROR_UNBLCKAGNTREC                        = API_ERROR_APIDEMON_BASE + 256;   /* sesUnbLckAgntRec */
    public static final int API_ERROR_UNBLCKENBUSRREC                      = API_ERROR_APIDEMON_BASE + 257;   /* sesUnbLckEnbUsrRec */
    public static final int API_ERROR_UNBLCKGRPMBRREC                      = API_ERROR_APIDEMON_BASE + 258;   /* sesUnbLckGrpMbrRec */
    public static final int API_ERROR_UNBLCKGRPREC                         = API_ERROR_APIDEMON_BASE + 259;   /* sesUnbLckGrpRec */
    public static final int API_ERROR_UNBLCKRPLTKN                         = API_ERROR_APIDEMON_BASE + 260;   /* sesUnbLckRplTkn */
    public static final int API_ERROR_UNBOPNCURS                           = API_ERROR_APIDEMON_BASE + 261;   /* sesUnbOpnCurs */
    public static final int API_ERROR_UNBPREPSB                            = API_ERROR_APIDEMON_BASE + 262;   /* sesUnbPrepSb */
    public static final int API_ERROR_UNBRANFILSOFTSEED                    = API_ERROR_APIDEMON_BASE + 263;   /* sesUnbRanFilSoftSeed */
    public static final int API_ERROR_UNBTOCONVPROMPT                      = API_ERROR_APIDEMON_BASE + 264;   /* sesUnbToConvPrompt */
    public static final int API_ERROR_UNBTODLTASNDTK                       = API_ERROR_APIDEMON_BASE + 265;   /* sesUnbToDltAsndTk */
    public static final int API_ERROR_UNBTOINSSIT                          = API_ERROR_APIDEMON_BASE + 266;   /* sesUnbToInsSit */
    public static final int API_ERROR_UNBTOLCKAGNTREC                      = API_ERROR_APIDEMON_BASE + 267;   /* sesUnbToLckAgntRec */
    public static final int API_ERROR_UNBTOLCKSITREC                       = API_ERROR_APIDEMON_BASE + 268;   /* sesUnbToLckSitRec */
    public static final int API_ERROR_UNBTOUPDADMRLTBL                     = API_ERROR_APIDEMON_BASE + 269;   /* sesUnbToUpdAdmRlTbl */
    public static final int API_ERROR_UNBUPDAGNT                           = API_ERROR_APIDEMON_BASE + 270;   /* sesUnbUpdAgnt */
    public static final int API_ERROR_UNBUPDGRPMBRREC                      = API_ERROR_APIDEMON_BASE + 271;   /* sesUnbUpdGrpMbrRec */
    public static final int API_ERROR_UNBUPDGRPREC                         = API_ERROR_APIDEMON_BASE + 272;   /* sesUnbUpdGrpRec */
    public static final int API_ERROR_UNBUPDSITREC                         = API_ERROR_APIDEMON_BASE + 273;   /* sesUnbUpdSitRec */
    public static final int API_ERROR_UNBWRTHDRINSOFTDB                    = API_ERROR_APIDEMON_BASE + 274;   /* sesUnbWrtHdrInSoftDB */
    public static final int API_ERROR_UPDTKNPAIR                           = API_ERROR_APIDEMON_BASE + 275;   /* sesUpdTknPair */
    public static final int API_ERROR_USRALRINDB                           = API_ERROR_APIDEMON_BASE + 276;   /* sesUsrAlrInDB */
    public static final int API_ERROR_USRHSADMPRIV                         = API_ERROR_APIDEMON_BASE + 277;   /* sesUsrHsAdmPriv */
    public static final int API_ERROR_USRHSASGNTKN                         = API_ERROR_APIDEMON_BASE + 278;   /* sesUsrHsAsgnTkn */
    public static final int API_ERROR_USRHSLDAPINF                         = API_ERROR_APIDEMON_BASE + 279;   /* sesUsrHsLDAPInf */
    public static final int API_ERROR_USRHSNOPROF                          = API_ERROR_APIDEMON_BASE + 280;   /* sesUsrHsNoProf */
    public static final int API_ERROR_USRISADM                             = API_ERROR_APIDEMON_BASE + 281;   /* sesUsrIsAdm */
    public static final int API_ERROR_USRISRMT                             = API_ERROR_APIDEMON_BASE + 282;   /* sesUsrIsRmt */
    public static final int API_ERROR_USRNTADMIN                           = API_ERROR_APIDEMON_BASE + 283;   /* sesUsrNtAdmin */
    public static final int API_ERROR_USRPNNTALWD                          = API_ERROR_APIDEMON_BASE + 284;   /* sesUsrPnNtAlwd */
    public static final int API_ERROR_GNRLAUTHFL                           = API_ERROR_APIDEMON_BASE + 285;   /* sesGnrlAuthFlr */
    public static final int API_ERROR_PINNOTMATCH                          = API_ERROR_APIDEMON_BASE + 286;   /* new */
    public static final int API_ERROR_HSTNMEXSTSNODE                       = API_ERROR_APIDEMON_BASE + 287;   /* merge 5.0.1 */
    public static final int API_ERROR_UNBADDSNODE                          = API_ERROR_APIDEMON_BASE + 288;   /* merge 5.0.1 */
    public static final int API_ERROR_INVSNODE                             = API_ERROR_APIDEMON_BASE + 289;   /* merge 5.0.1 */
    public static final int API_ERROR_UNBLCKSNODE                          = API_ERROR_APIDEMON_BASE + 290;   /* merge 5.0.1 */
    public static final int API_ERROR_UNBDLTSNODE                          = API_ERROR_APIDEMON_BASE + 291;   /* merge 5.0.1 */
    public static final int API_ERROR_XMLCONTEXT                           = API_ERROR_APIDEMON_BASE + 292;   /* new */
    public static final int API_ERROR_XMLOPEN                              = API_ERROR_APIDEMON_BASE + 293;   /* new */
    public static final int API_ERROR_XMLEMPTY                             = API_ERROR_APIDEMON_BASE + 294;   /* new */
    public static final int API_ERROR_XMLCLOSE                             = API_ERROR_APIDEMON_BASE + 295;   /* new */
    public static final int API_ERROR_XMLEMERGCLOSE                        = API_ERROR_APIDEMON_BASE + 296;   /* new */
    public static final int API_ERROR_XMLNOUPDATECLOSE                     = API_ERROR_APIDEMON_BASE + 297;   /* new */
    public static final int API_ERROR_INVALIDSWNICK                        = API_ERROR_APIDEMON_BASE + 298;   /* new */
    public static final int API_ERROR_INVUSRSTART                          = API_ERROR_APIDEMON_BASE + 299;   /* new */
    public static final int API_ERROR_INVUSREND                            = API_ERROR_APIDEMON_BASE + 300;   /* new */
    public static final int API_ERROR_INVUSRRNGE                           = API_ERROR_APIDEMON_BASE + 301;   /* new */
    public static final int API_ERROR_POOLLOCK                             = API_ERROR_APIDEMON_BASE + 302;   /* new */
    public static final int API_ERROR_INVSTMT                              = API_ERROR_APIDEMON_BASE + 303;   /* new */
    public static final int API_ERROR_FLDEXEC                              = API_ERROR_APIDEMON_BASE + 304;   /* new */
    public static final int API_ERROR_NOPOOL                               = API_ERROR_APIDEMON_BASE + 305;   /* new */
    public static final int API_ERROR_INVPOOLNAME                          = API_ERROR_APIDEMON_BASE + 306;   /* new */
    public static final int API_ERROR_FLDREGPOOL                           = API_ERROR_APIDEMON_BASE + 307;   /* new */
    public static final int API_ERROR_USERLOCK                             = API_ERROR_APIDEMON_BASE + 308;   /* new */
    public static final int API_ERROR_DEMONLOCK                            = API_ERROR_APIDEMON_BASE + 309;   /* new */
    public static final int API_ERROR_POOLREAD                             = API_ERROR_APIDEMON_BASE + 310;   /* new */
    public static final int API_ERROR_POOLWRITE                            = API_ERROR_APIDEMON_BASE + 311;   /* new */
    public static final int API_ERROR_INVLEN                               = API_ERROR_APIDEMON_BASE + 312;   /* new */
    public static final int API_ERROR_POOLBINREQ                           = API_ERROR_APIDEMON_BASE + 313;   /* new */
    public static final int API_ERROR_POOLNONBINREQ                        = API_ERROR_APIDEMON_BASE + 314;   /* new */
    public static final int API_ERROR_SITRECNOTFND                         = API_ERROR_APIDEMON_BASE + 315;   /* constructed */
    public static final int API_ERROR_OPNSITEXT                            = API_ERROR_APIDEMON_BASE + 316;   /* constructed */
    public static final int API_ERROR_FTCHSITEXT                           = API_ERROR_APIDEMON_BASE + 317;   /* constructed */
    public static final int API_ERROR_INSSITEXT                            = API_ERROR_APIDEMON_BASE + 318;   /* constructed */
    public static final int API_ERROR_LKSITEXT                             = API_ERROR_APIDEMON_BASE + 319;   /* constructed */
    public static final int API_ERROR_UPDSITEXT                            = API_ERROR_APIDEMON_BASE + 320;   /* constructed */
    public static final int API_ERROR_DELSITEXT                            = API_ERROR_APIDEMON_BASE + 321;   /* constructed */
    public static final int API_ERROR_CLNTRECNOTFND                        = API_ERROR_APIDEMON_BASE + 322;   /* constructed */
    public static final int API_ERROR_OPNCLNTEXT                           = API_ERROR_APIDEMON_BASE + 323;   /* constructed */
    public static final int API_ERROR_FTCHCLNTEXT                          = API_ERROR_APIDEMON_BASE + 324;   /* constructed */
    public static final int API_ERROR_INSCLNTEXT                           = API_ERROR_APIDEMON_BASE + 325;   /* constructed */
    public static final int API_ERROR_LKCLNTEXT                            = API_ERROR_APIDEMON_BASE + 326;   /* constructed */
    public static final int API_ERROR_UPDCLNTEXT                           = API_ERROR_APIDEMON_BASE + 327;   /* constructed */
    public static final int API_ERROR_DELCLNTEXT                           = API_ERROR_APIDEMON_BASE + 328;   /* constructed */
    public static final int API_ERROR_GRPRECNOTFND                         = API_ERROR_APIDEMON_BASE + 329;   /* constructed */
    public static final int API_ERROR_OPNGRPEXT                            = API_ERROR_APIDEMON_BASE + 330;   /* constructed */
    public static final int API_ERROR_FTCHGRPEXT                           = API_ERROR_APIDEMON_BASE + 331;   /* constructed */
    public static final int API_ERROR_INSGRPEXT                            = API_ERROR_APIDEMON_BASE + 332;   /* constructed */
    public static final int API_ERROR_LKGRPEXT                             = API_ERROR_APIDEMON_BASE + 333;   /* constructed */
    public static final int API_ERROR_UPDGRPEXT                            = API_ERROR_APIDEMON_BASE + 334;   /* constructed */
    public static final int API_ERROR_DELGRPEXT                            = API_ERROR_APIDEMON_BASE + 335;   /* constructed */
    public static final int API_ERROR_USRRECNOTFND                         = API_ERROR_APIDEMON_BASE + 336;   /* constructed */
    public static final int API_ERROR_OPNUSREXT                            = API_ERROR_APIDEMON_BASE + 337;   /* constructed */
    public static final int API_ERROR_FTCHUSREXT                           = API_ERROR_APIDEMON_BASE + 338;   /* constructed */
    public static final int API_ERROR_INSUSREXT                            = API_ERROR_APIDEMON_BASE + 339;   /* constructed */
    public static final int API_ERROR_LKUSREXT                             = API_ERROR_APIDEMON_BASE + 340;   /* constructed */
    public static final int API_ERROR_UPDUSREXT                            = API_ERROR_APIDEMON_BASE + 341;   /* constructed */
    public static final int API_ERROR_DELUSREXT                            = API_ERROR_APIDEMON_BASE + 342;   /* constructed */
    public static final int API_ERROR_TKNRECNOTFND                         = API_ERROR_APIDEMON_BASE + 343;   /* constructed */
    public static final int API_ERROR_OPNTKNEXT                            = API_ERROR_APIDEMON_BASE + 344;   /* constructed */
    public static final int API_ERROR_FTCHTKNEXT                           = API_ERROR_APIDEMON_BASE + 345;   /* constructed */
    public static final int API_ERROR_INSTKNEXT                            = API_ERROR_APIDEMON_BASE + 346;   /* constructed */
    public static final int API_ERROR_LKTKNEXT                             = API_ERROR_APIDEMON_BASE + 347;   /* constructed */
    public static final int API_ERROR_UPDTKNEXT                            = API_ERROR_APIDEMON_BASE + 348;   /* constructed */
    public static final int API_ERROR_DELTKNEXT                            = API_ERROR_APIDEMON_BASE + 349;   /* constructed */
    public static final int API_ERROR_OPNSYSEXT                            = API_ERROR_APIDEMON_BASE + 350;   /* constructed */
    public static final int API_ERROR_FTCHSYSEXT                           = API_ERROR_APIDEMON_BASE + 351;   /* constructed */
    public static final int API_ERROR_INSSYSEXT                            = API_ERROR_APIDEMON_BASE + 352;   /* constructed */
    public static final int API_ERROR_LKSYSEXT                             = API_ERROR_APIDEMON_BASE + 353;   /* constructed */
    public static final int API_ERROR_UPDSYSEXT                            = API_ERROR_APIDEMON_BASE + 354;   /* constructed */
    public static final int API_ERROR_DELSYSEXT                            = API_ERROR_APIDEMON_BASE + 355;   /* constructed */
    public static final int API_ERROR_READLIC                              = API_ERROR_APIDEMON_BASE + 356;   /* new */
    public static final int API_ERROR_OPNJOBTBL                            = API_ERROR_APIDEMON_BASE + 357;   /* new */
    public static final int API_ERROR_FCHJOBTBL                            = API_ERROR_APIDEMON_BASE + 358;   /* new */
    public static final int API_ERROR_GETJOBTBL                            = API_ERROR_APIDEMON_BASE + 359;   /* new */
    public static final int API_ERROR_INVJOB                               = API_ERROR_APIDEMON_BASE + 360;   /* new */
    public static final int API_ERROR_MODIFYTOKEN                          = API_ERROR_APIDEMON_BASE + 361;   /* new */
    public static final int API_ERROR_MODIFYUSER                           = API_ERROR_APIDEMON_BASE + 362;   /* new */
    public static final int API_ERROR_VERSION_MISMATCH                     = API_ERROR_APIDEMON_BASE + 363;   /* new */
    public static final int API_ERROR_CHGSFTTKNAUTHWITH                    = API_ERROR_APIDEMON_BASE + 364;   /* new */
    public static final int API_ERROR_CHGRPLMTKNAUTHWITH                   = API_ERROR_APIDEMON_BASE + 365;   /* new */
    public static final int API_ERROR_CHGTKNAUTHWITH                       = API_ERROR_APIDEMON_BASE + 366;   /* new */
    public static final int API_ERROR_INVTKN_128BIT                        = API_ERROR_APIDEMON_BASE + 367;   /* 5.2 */
    public static final int API_ERROR_INVTKN_TKNCODEONLY                   = API_ERROR_APIDEMON_BASE + 368;   /* 5.2 */
    public static final int API_ERROR_INVAUTHWITH                          = API_ERROR_APIDEMON_BASE + 369;   /* 5.2 */
    public static final int API_ERROR_GETVERSION                           = API_ERROR_APIDEMON_BASE + 370;   /* 5.2 */
    public static final int API_ERROR_LDAPTMPNA                            = API_ERROR_APIDEMON_BASE + 371;   /* 5.2 */
    public static final int API_ERROR_EXTFMT                               = API_ERROR_APIDEMON_BASE + 372;   /* 5.2 */
    public static final int API_ERROR_XMLTAG                               = API_ERROR_APIDEMON_BASE + 373;   /* 5.2 */
    public static final int API_ERROR_RETRFILENAME                         = API_ERROR_APIDEMON_BASE + 374;   /* 5.2 */
    public static final int API_ERROR_INVFMT                               = API_ERROR_APIDEMON_BASE + 375;   /* 5.2 */
    public static final int API_ERROR_BADPINTYPE                           = API_ERROR_APIDEMON_BASE + 376;   /* 5.2 */
    public static final int API_ERROR_PINTOOBIGORBLANK                     = API_ERROR_APIDEMON_BASE + 377;   /* 5.2 */
    public static final int API_ERROR_DCRYUSERSECBLK                       = API_ERROR_APIDEMON_BASE + 378;   /* 6.0 Disconneted Auth */
    public static final int API_ERROR_ENCRUSERSECBLK                       = API_ERROR_APIDEMON_BASE + 379;   /* 6.0 Disconneted Auth */
    public static final int API_ERROR_IVNDALOGINPWNA                       = API_ERROR_APIDEMON_BASE + 380;   /* 6.0 Disconneted Auth */
    public static final int API_ERROR_IVNDALOGINPWTOKENNA                  = API_ERROR_APIDEMON_BASE + 381;   /* 6.0 Disconneted Auth */
    public static final int API_ERROR_SYSPARAMOAOFF                        = API_ERROR_APIDEMON_BASE + 382;   /* 6.0 Disconneted Auth */
    public static final int API_ERROR_SYSPARAMPIOFF                        = API_ERROR_APIDEMON_BASE + 383;   /* 6.0 Disconneted Auth */
    public static final int API_ERROR_INVALIDLTPEXP                        = API_ERROR_APIDEMON_BASE + 384;   /* 6.1 */
    public static final int API_ERROR_CANTSETOADAYS                        = API_ERROR_APIDEMON_BASE + 385;   /* 6.1 */
    public static final int API_ERROR_OADAYSLESSTHANSYS                    = API_ERROR_APIDEMON_BASE + 386;   /* 6.1 */
    public static final int API_ERROR_INVPARTKNDEPLOY                      = API_ERROR_APIDEMON_BASE + 387;   /* 6.1 */
    public static final int API_ERROR_INVRADCONNPORT                       = API_ERROR_APIDEMON_BASE + 388;   /* 6.1 */
    public static final int API_ERROR_INVPORTPRIMCOMB                      = API_ERROR_APIDEMON_BASE + 389;   /* 6.1 */
    public static final int API_ERROR_EMPTYRADAGNTNAME                     = API_ERROR_APIDEMON_BASE + 390;   /* 6.1 */
    public static final int API_ERROR_TOOLONGHSTNAME                       = API_ERROR_APIDEMON_BASE + 391;   /* 6.1 */
    public static final int API_ERROR_INVALIPALIAS                         = API_ERROR_APIDEMON_BASE + 392;   /* 6.1 */
    public static final int API_ERROR_INVALTOKTYPE                         = API_ERROR_APIDEMON_BASE + 393;   /* 6.1 */
    public static final int API_ERROR_RADIUSPRIMARYEXISTS                  = API_ERROR_APIDEMON_BASE + 394;   /* 6.1 */

    /* DO NOT FORGET TO SET THIS CONSTANT WHEN YOU ADD ERROR MESSAGES TO ATK */
    public static final int API_MAX_ERROR_ARRAY                    = 395;

    /* Non lookup erros - not mapped to any static string       */
    /* The error string is constructed in the code              */
    /* The following two errors are returned only by Sd_ApiInit */
    /* and Sd_ApiInitSingle                                     */
    public static final int API_ERROR_FLDCONNSRV                   = API_ERROR_APIDEMON_NLBASE + 1;
    public static final int API_ERROR_FLDCONNLOG                   = API_ERROR_APIDEMON_NLBASE + 2;

}
