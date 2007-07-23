//
// $Id: DateWidget.js,v 1.11 2006/04/25 23:55:03 callahan Exp $
//
// Javascript methods to handle date selection widgets and 
// take care of things like # days per month and beginning
// and ending years with fewer than 12 months.
// 


////////////////////////////////////////////////////////////
//                                                        //
// Define methods of the DateWidget object                //
//                                                        //
////////////////////////////////////////////////////////////

// During initialization, DateLo, DateHi, Date1 and Date2
// must all be set before creating the first menu.  That's
// why we can't reuse the code in the setDate1/Hi methods.
function DateWidget_initialize(String1,String2) {

  var date1 = this.parseDate(String1);
  var date2 = this.parseDate(String2);

// Set the time domain

  this.yearLo = date1[0];
  this.monthLo = date1[1];
  this.dayLo = date1[2];
  this.hourLo = date1[3];
  this.minuteLo = date1[4];
  this.secondLo = date1[5];
  this.yearHi = date2[0];
  this.monthHi = date2[1];
  this.dayHi = date2[2];
  this.hourHi = date2[3];
  this.minuteHi = date2[4];
  this.secondHi = date2[5];

// Set the currently selected time range = full domain

  this.year1 = this.yearLo;
  this.month1 = this.monthLo;
  this.day1 = this.dayLo;
  this.hour1 = this.hourLo;
  this.minute1 = this.minuteLo;
  this.second1 = this.secondLo;
  this.year2 = this.yearHi;
  this.month2 = this.monthHi;
  this.day2 = this.dayHi;
  this.hour2 = this.hourHi;
  this.minute2 = this.minuteHi;
  this.second2 = this.secondHi;

  this.initializing = 1;
  this.createYearMenu(this.form.Year1);
  if (this.form.Year2) {
    this.initializing = 1;
    this.createYearMenu(this.form.Year2);
  }
}

function DateWidget_setDateRange(String1,String2) {
  this.resetDateRange();
  this.setDate1(String1);
  this.setDate2(String2);
}

// NOTE: If (! Date1 <= String1 <= Date2 ) this routine
// NOTE: will not work properly.  The internal logic will
// NOTE: prevent this from happening but any resetting of
// NOTE: dates from the outside should ALWAYS use setDateRange().
function DateWidget_setDate1(String1) {
  var date1 = this.parseDate(String1);
  this.year1 = date1[0];
  this.month1 = date1[1];
  this.day1 = date1[2];
  this.hour1 = date1[3];
  this.minute1 = date1[4];
  this.second1 = date1[5];
  this.createYearMenu(this.form.Year1);
}

function DateWidget_setDate2(String2) {
  var date2 = this.parseDate(String2);
  this.year2 = date2[0];
  this.month2 = date2[1];
  this.day2 = date2[2];
  this.hour2 = date2[3];
  this.minute2 = date2[4];
  this.second2 = date2[5];
  if (this.form.Year2) {
    this.createYearMenu(this.form.Year2);
  }
}

// Recreate the Year menu
// RESTRICT_OPTIONS: 
//   If widget #1, range = yearLo:year2
//   If widget #2, range = year1:yearHi
// DEFAULT: 
//   If widget #1, range = yearLo:yearHi
//   If widget #2, range = yearLo:yearHi
function DateWidget_createYearMenu(YearMenu) {
  if (typeof YearMenu == "undefined") 
    return;

  var loYear;
  var hiYear;
  var currentYear;

// Get the appropriate loYear, hiYear and currentYear

  if (YearMenu.name == 'Year1') {
    loYear = this.yearLo;
    hiYear = (this.mode == 'RESTRICT_OPTIONS') ? this.year2 : this.yearHi;
    currentYear = (this.year1<=this.year2) ? this.year1 : this.year2;
  } else {
    loYear = (this.mode == 'RESTRICT_OPTIONS') ? this.year1 : this.yearLo;
    hiYear = this.yearHi;
    currentYear = (this.year1<=this.year2) ? this.year2 : this.year1;
  }

// Create a new set of options and then select
// a year: current selection or nearest available unless initializing.

  var n = hiYear - loYear + 1
  var y = loYear;
  with (YearMenu) {
    options.length=0;
    if (this.yearLo == '0000' || this.yearLo == '0001' ) {
      options[0]=new Option('Climatology',y);
    } else {
      for (i=0; i<n; i++) {
        options[i]=new Option(y,y);
        y++;  
      }
    }
    if (this.initializing) {
      this.internallyForced = 1;
      if (name == 'Year1') {
        options[0].selected=true;
      } else {
        options[length-1].selected=true;
      }
      this.initializing = 0;
    } else {
      y = loYear;
      for (i=0; i<n; i++) {
        if (y == currentYear) {
          options[i].selected=true;
        }
        y++;  
      }
    }
  }
  this.selectYear(YearMenu);

}


function DateWidget_selectYear(YearMenu) {

  var monthNames = new Array('Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec');
  var monthVals = new Array('01','02','03','04','05','06','07','08','09','10','11','12');

  currentYear = YearMenu.options[YearMenu.selectedIndex].value;

  var currentMonth;
  var MonthMenu;
  if (YearMenu.name == 'Year1') {
    this.year1 = currentYear;
    currentMonth = (this.year1 == this.year2 && 
                    this.month1 > this.month2) ? this.month2 - 1 : this.month1 - 1;
    MonthMenu = this.form.Month1;
  } else {
    this.year2 = currentYear;
    currentMonth = (this.year1 == this.year2 && 
                    this.month1 > this.month2) ? this.month1 - 1 : this.month2 - 1;
    MonthMenu = this.form.Month2;
  }

// Recreate the Month menu
// RESTRICT_OPTIONS: 
//   If widget #1, range = yearLo-monthLo:year2-month2
//   If widget #2, range = year1-month1:yearHi-monthHi
// DEFAULT: 
//   If widget #1, range = yearLo-monthLo:yearHi-monthHi
//   If widget #2, range = yearLo-monthLo:yearHi-monthHi

  var loMonth = 0;
  var hiMonth = 11;
  var OtherYearMenu;
  if (YearMenu.name == 'Year1') {
    OtherYearMenu = this.form.Year2;
    if (currentYear == this.yearLo) {
      loMonth = this.monthLo - 1; 
    }
    if (this.mode == 'RESTRICT_OPTIONS') {
      if (currentYear == this.year2) {
        hiMonth = this.month2 - 1; 
      }
    } else {
      if (currentYear == this.yearHi) {
        hiMonth = this.monthHi - 1; 
      }
    }
  } else {
    OtherYearMenu = this.form.Year1;
    if (this.mode == 'RESTRICT_OPTIONS') {
      if (currentYear == this.year1) {
        loMonth = this.month1 - 1; 
      }
    } else {
      if (currentYear == this.yearLo) {
        loMonth = this.monthLo - 1; 
      }
    }
    if (currentYear == this.yearHi) {
      hiMonth = this.monthHi - 1; 
    }
  }

// Create a new set of options and then select
// a month: current selection or nearest available unless initializing.

  if (MonthMenu) {
    with (MonthMenu) {
      var n = hiMonth - loMonth;
      var m = loMonth;
      options.length=0;
      if (this.monthLo != '00') {
        for (i=0; i<=n; i++) {
          options[i]=new Option(monthNames[m],monthVals[m]);
          m++;
        }
        if (currentMonth < loMonth || currentMonth > hiMonth) {
          if (currentMonth < loMonth) {
            options[0].selected = true;
           } else {
            var i = hiMonth - loMonth;
            options[i].selected = true;
           }
        } else {
          var i = currentMonth - loMonth;
          options[i].selected = true;
        }
      }
    }
    this.selectMonth(MonthMenu);
  } else { 
//No Month-Day-Time Menus
    if (this.internallyForced) {
      this.internallyForced = 0;
    } else {
      if (OtherYearMenu) {
        if (this.mode == 'RESTRICT_OPTIONS' || !this.correctOrder()) {
          this.internallyForced = 1;
          this.createYearMenu(OtherYearMenu);
        }
      }
    }
  }

}


function DateWidget_selectMonth(MonthMenu) {

  var currentYear;
  var currentMonth; 
  var currentDay;
  var DayMenu;
  var OtherYearMenu;

  var dayVals = new Array('01','02','03','04','05','06','07','08','09','10',
                          '11','12','13','14','15','16','17','18','19','20',
                          '21','22','23','24','25','26','27','28','29','30','31');

  if (this.monthLo == '00') {
    currentMonth = '00';
  } else {
    currentMonth = MonthMenu.options[MonthMenu.selectedIndex].value;
  }

  if (MonthMenu.name == 'Month1') {
    OtherYearMenu = this.form.Year2;
    currentYear = this.year1;
    this.month1 = currentMonth;
    if (this.mode == 'RESTRICT_OPTIONS') {
      currentDay = this.day1;
    } else {
      currentDay = (this.year1 == this.year2 && 
                    this.month1 == this.month2 && 
                    this.day1 > this.day2) ? this.day2 : this.day1;
    }
    DayMenu = this.form.Day1;
  } else {
    OtherYearMenu = this.form.Year1;
    currentYear = this.year2;
    this.month2 = currentMonth;
    if (this.mode == 'RESTRICT_OPTIONS') {
      currentDay = this.day2;
    } else {
      currentDay = (this.year1 == this.year2 && 
                    this.month1 == this.month2 && 
                    this.day1 > this.day2) ? this.day1 : this.day2;
    }
    DayMenu = this.form.Day2;
  }

// Recreate the Day menu

  var loDay = 1;
  var hiDay = 31;

// Modify hiDay based on month

  switch (currentMonth) {
    case "02":
      hiDay = this.febDays(currentYear); break;
    case "04":
      hiDay = 30; break;
    case "06":
      hiDay = 30; break;
    case "09":
      hiDay = 30; break;
    case "11":
      hiDay = 30; break;
    default:
      hiDay = 31; break;
  }

// Recreate the Day menu
// RESTRICT_OPTIONS: 
//   If widget #1, range = yearLo-monthLo-dayLo:year2-month2-day2
//   If widget #2, range = year1-month1-day1:yearHi-monthHi-dayHi
// DEFAULT: 
//   If widget #1, range = yearLo-monthLo-dayLo:yearHi-monthHi-dayHi
//   If widget #2, range = yearLo-monthLo-dayLo:yearHi-monthHi-dayHi

  if (MonthMenu.name == 'Month1') {
   if (currentYear == this.yearLo && currentMonth == this.monthLo) {
     loDay = this.dayLo; 
   }
   if (this.mode == 'RESTRICT_OPTIONS') {
     if (currentYear == this.year2 && currentMonth == this.month2) {
       hiDay = this.day2; 
     }
   } else {
     if (currentYear == this.yearHi && currentMonth == this.monthHi) {
       hiDay = this.dayHi; 
     }
   }
  } else {
   if (this.mode == 'RESTRICT_OPTIONS') {
     if (currentYear == this.year1 && currentMonth == this.month1) {
       loDay = this.day1; 
     }
   } else {
     if (currentYear == this.yearLo && currentMonth == this.monthLo) {
       loDay = this.dayLo; 
     }
   }
   if (currentYear == this.yearHi && currentMonth == this.monthHi) {
     hiDay = this.dayHi; 
   }
  }

// Create a new set of options and then select
// a day: current selection or nearest available

  if (DayMenu) {
    with (DayMenu) {
      var n = hiDay - loDay + 1;
      var d = loDay - 1;
      options.length=0;
      if (this.dayLo != '00') { // No days widget
        for (i=0; i<n; i++) {
          options[i]=new Option(dayVals[d],dayVals[d]);
          d++;
        }
        if (currentDay < loDay || currentDay > hiDay) {
          if (currentDay < loDay) {
            options[0].selected = true;
          } else {
            var i = hiDay - loDay;
            options[i].selected = true;
          }
        } else {
          var i = currentDay - loDay;
          options[i].selected = true;
        }
      }
    }
    this.selectDay(DayMenu);
  } else {
//No Day-Time Menus
    if (this.internallyForced) {
      this.internallyForced = 0;
    } else {
      if (OtherYearMenu) {
        if (this.mode == 'RESTRICT_OPTIONS' || !this.correctOrder()) {
          this.internallyForced = 1;
          this.createYearMenu(OtherYearMenu);
        }
      }
    }
  } 

}


function DateWidget_selectDay(DayMenu) {

  var currentYear;
  var currentMonth;
  var currentDay;
  var currentHour;
  var TimeMenu;
  var OtherYearMenu;

  if (this.dayLo == '00') { // No days
    currentDay = '00';
  } else {
    currentDay = DayMenu.options[DayMenu.selectedIndex].value;
  }
  if (DayMenu.name == 'Day1') {
// TODO: handle RESTRICT_OPTIONS for Time menu
    this.day1 = currentDay;
    TimeMenu = this.form.Time1;
    OtherYearMenu = this.form.Year2;
    currentYear = this.year1;
    currentMonth = this.month1;
    currentDay = this.day1;
    currentTime = (currentYear == this.year2 && 
                   currentMonth == this.month2 && 
                   currentDay == this.day2 &&
                   this.hour1 > this.hour2) ? 
                   this.hour2 + ':' + this.minute2 :
                   this.hour1 + ':' + this.minute1;
  } else {
    this.day2 = currentDay;
    TimeMenu = this.form.Time2;
    OtherYearMenu = this.form.Year1;
    currentYear = this.year2;
    currentMonth = this.month2;
    currentDay = this.day2;
    currentTime = (currentYear == this.year1 && 
                   currentMonth == this.month1 && 
                   currentDay == this.day1 &&
                   this.hour1 > this.hour2) ? 
                   this.hour1 + ':' + this.minute1 :
                   this.hour2 + ':' + this.minute2;
  }
  var loMinute = this.offsetMinutes;
  var hiMinute = 1440;
//TODO: handle RESTRICT_OPTIONS for Time menu
// NOTE: use Number() to prevent '60' + '60' = '6060'
  if (currentYear == this.yearLo && 
     currentMonth == this.monthLo &&
     currentDay == this.dayLo) {
    loMinute = Number(this.minuteLo) + Number(this.hourLo * 60);
  }
  if (currentYear == this.yearHi && 
     currentMonth == this.monthHi &&
     currentDay == this.dayHi) {
     hiMinute = Number(this.minuteHi) + Number(this.hourHi * 60);
  }

// Create hours menu

  if (TimeMenu) {
    with (TimeMenu) {
      var time='00:00';
      var hr = Math.floor(loMinute/60);
      var min = loMinute - hr * 60;
      var minutes = loMinute;
      var n = Math.floor((hiMinute-loMinute)/this.deltaMinutes) + 1;
      for (i=0; i<n; i++) {
        time = this.twoDigit(hr) + ":" + this.twoDigit(min);
        options[i]=new Option(time,time);
        if (time == currentTime) {
          options[i].selected = true;
        }
        minutes = Number(minutes) + Number(this.deltaMinutes);
        hr = Math.floor(minutes/60);
        min = minutes - hr * 60;
        if (hr >= 24) { break; }
      }
    }
  }

// Force update of other Years, Months & Days while avoiding an infinite loop

  if (this.internallyForced) {
    this.internallyForced = 0;
  } else {
    if (OtherYearMenu) {
      if (this.mode == 'RESTRICT_OPTIONS' || !this.correctOrder()) {
        this.internallyForced = 1;
        this.createYearMenu(OtherYearMenu);
      }
    }
  }

}

function DateWidget_selectTime(TimeMenu) {
  var time = TimeMenu.options[TimeMenu.selectedIndex].value;
  var Time = String(time).split(':');
    
  if (TimeMenu.name == 'Time1') {
    this.hour1 = Time[0];    
    this.minute1 = Time[1];
  } else {
    this.hour2 = Time[0];    
    this.minute2 = Time[1];
  }
}

function DateWidget_toString() {
  var message = 'dateLo = ' + this.getDateLo() + '\n' +
                'dateHi = ' + this.getDateHi() + '\n' +
                'date1 = ' + this.getDate1() + '\n' +
                'date2 = ' + this.getDate2() + '\n' +
                'this.mode = ' + this.mode + '\n' +
                'this.deltaMinutes = ' + this.deltaMinutes + '\n' +
                'this.offsetMinutes = ' + this.offsetMinutes + '\n';
  return message;
}

function DateWidget_getDateLo() {
  var monthLo = (this.monthLo == '00') ? '' : '-' + this.monthLo;
  var dayLo = (this.dayLo == '00') ? '' : '-' + this.dayLo;
  var timeLo = (this.form.Time1) ? ' ' + this.hourLo + ':' + this.minuteLo + ':' + this.secondLo : '';
  var message = this.yearLo + monthLo + dayLo + timeLo;
  return message;
}

function DateWidget_getDateHi() {
  var monthHi = (this.monthHi == '00') ? '' : '-' + this.monthHi;
  var dayHi = (this.dayHi == '00') ? '' : '-' + this.dayHi;
  var timeHi = (this.form.Time2) ? ' ' + this.hourHi + ':' + this.minuteHi + ':' + this.secondHi : '';
  var message = this.yearHi + monthHi + dayHi + timeHi;
  return message;
}

function DateWidget_getDate1() {
  var month1 = (this.month1 == '00') ? '' : '-' + this.month1;
  var day1 = (this.day1 == '00') ? '' : '-' + this.day1;
  var time1 = (this.form.Time1) ? ' ' + this.hour1 + ':' + this.minute1 + ':' + this.second1 : '';
  var message = this.year1 + month1 + day1 + time1;
  return message;
}

function DateWidget_getDate2() {
  var month2 = (this.month2 == '00') ? '' : '-' + this.month2;
  var day2 = (this.day2 == '00') ? '' : '-' + this.day2;
  var time2 = (this.form.Time2) ? ' ' + this.hour2 + ':' + this.minute2 + ':' + this.second2 : '';
  var message = (this.form.Year2) ? this.year2 + month2 + day2 + time2 : '';
  return message;
}

function DateWidget_getDate1_Ferret() {
  var monthNames = new Array('Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec');
  var month1 = (this.month1 == '00') ? '' : '-' + monthNames[Number(this.month1-1)];
  var day1 = (this.day1 == '00') ? '' : this.day1;
  var time1 = (this.form.Time1) ? ' ' + this.hour1 + ':' + this.minute1 + ':' + this.second1 : '';
  var message = day1 + month1 + '-' + this.year1 + time1;
  return message;
}

function DateWidget_getDate2_Ferret() {
  var monthNames = new Array('Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec');
  var month2 = (this.month2 == '00') ? '' : '-' + monthNames[Number(this.month2-1)];
  var day2 = (this.day2 == '00') ? '' : this.day2;
  var time2 = (this.form.Time2) ? ' ' + this.hour2 + ':' + this.minute2 + ':' + this.second2 : '';
  var message = (this.form.Year2) ? day2 + month2 + '-' + this.year2 + time2 : '';
  return message;
}

function DateWidget_alert() {
  alert(this.toString());
}

function DateWidget_resetDate1() {
  this.setDate1(this.getDateLo());
}

function DateWidget_resetDate2() {
  this.setDate2(this.getDateHi());
}

function DateWidget_resetDateRange() {
  this.resetDate1();
  this.resetDate2();
}

function DateWidget_setDeltaMinutes(min) {
  this.deltaMinutes = min;
}

function DateWidget_setOffsetMinutes(min) {
  this.offsetMinutes = min;
}

function DateWidget_setMode(mode) {
  this.mode = mode;
}

////////////////////////////////////////////////////////////
//                                                        //
// Internal methods                                       //
//                                                        //
////////////////////////////////////////////////////////////

function DateWidget_parseDate(dateString) {
  var YMDHMS; 

  switch (dateString) {
    case 'TODAY':
      var today = new Date();
      with (today) {
        YMDHMS = new Array(getFullYear(),getMonth()+1,getDate(),getHours(),getMinutes(),getSeconds());
      }
      break;
    case 'YESTERDAY':
      var today = new Date();
      var milli = today.valueOf() - 86400000;
      var yesterday = new Date(milli);
      with (yesterday) {
        YMDHMS = new Array(getFullYear(),getMonth()+1,getDate(),getHours(),getMinutes(),getSeconds());
      }
      break;
    default:
      var dateTime = String(dateString).split(' ');
      YMDHMS = String(dateTime[0]).split('-');  
      if (dateTime[1]) {
        var HMS = String(dateTime[1]).split(':');
        YMDHMS[3] = HMS[0];
        YMDHMS[4] = HMS[1];
        YMDHMS[5] = HMS[2];
      } else {
        YMDHMS[3] = '00';
        YMDHMS[4] = '00';
        YMDHMS[5] = '00';
      }
      break;
  }

// NOTE: no support for seconds at this time
  YMDHMS[5] = '00';
  return YMDHMS;
}

function DateWidget_febDays(year) {
  var febDays = 28 
  if ( (year%4) == 0 ) {
    febDays = 29;
    if ( (year%100) == 0 ) {
      febDays = 28;
      if ( (year%400) == 0 ) {
        febDays = 29;
      }
    }
  }
// Climatological February has 28 days
  if (year == '0000' || year == '0001' ) { febDays = 28; }
  return febDays;
}

function DateWidget_twoDigit(num) {
  if (String(num).length == 1) {
    num = '0' + num;
  }
  return num;
}

function DateWidget_correctOrder() {
  var order = 1;
  if (this.year1 > this.year2) {
    order = 0;
  } else {
    if (this.year1 == this.year2) {
      if (this.month1 > this.month2) {
        order = 0;
      } else {
        if (this.month1 == this.month2) {
          if (this.day1 > this.day2) {
            order = 0;
          } else {
            if (this.day1 == this.day2) {
              if (this.hour1 > this.hour2) {
                order = 0;
              }
            }
          }
        }
      }
    }
  }
  return order;
}

////////////////////////////////////////////////////////////
//                                                        //
// Define the DateWidget object                           //
//                                                        //
////////////////////////////////////////////////////////////

function DateWidget(form,lo,hi,deltaMinutes,offsetMinutes) {

// Public functions

  this.initialize = DateWidget_initialize;
  this.createYearMenu = DateWidget_createYearMenu;
  this.selectYear = DateWidget_selectYear;
  this.selectMonth = DateWidget_selectMonth;
  this.selectDay = DateWidget_selectDay;
  this.selectTime = DateWidget_selectTime;
  this.setDateRange = DateWidget_setDateRange;
  this.setDate1 = DateWidget_setDate1;
  this.setDate2 = DateWidget_setDate2;
  this.resetDateRange = DateWidget_resetDateRange;
  this.resetDate1 = DateWidget_resetDate1;
  this.resetDate2 = DateWidget_resetDate2;
  this.toString = DateWidget_toString;
  this.getDateLo = DateWidget_getDateLo;
  this.getDateHi = DateWidget_getDateHi;
  this.getDate1 = DateWidget_getDate1;
  this.getDate2 = DateWidget_getDate2;
  this.alert = DateWidget_alert;
  this.setDeltaMinutes = DateWidget_setDeltaMinutes;
  this.setOffsetMinutes = DateWidget_setOffsetMinutes;
  this.setMode = DateWidget_setMode;

// LAS/Ferret specific functions

  this.getDate1_Ferret = DateWidget_getDate1_Ferret;
  this.getDate2_Ferret = DateWidget_getDate2_Ferret;

// Internal functions

  this.parseDate = DateWidget_parseDate;
  this.febDays = DateWidget_febDays;
  this.twoDigit = DateWidget_twoDigit;
  this.correctOrder = DateWidget_correctOrder;

// Initialization

  this.form = form;
  this.mode = 'DEFAULT';
  this.deltaMinutes = (deltaMinutes) ? deltaMinutes : 1440;
  this.offsetMinutes = (offsetMinutes) ? offsetMinutes : 0;
  this.initialize(lo,hi);

}

