//
//  HealthKitModule.swift
//  Alwayz
//
//  Created by Snow on 8/27/23.
//

import Foundation
import HealthKit
import WatchConnectivity

@objc class HealthKitModule: NSObject {
  
  @objc static let healthKitSahred = HealthKitModule();
  
  var watchSession : WCSession!
  let healthStore = HKHealthStore()
  
  
  @objc func getStepsCount(start:Date, end:Date, completion: @escaping (Double) -> Void) {
    
    watchSession = WCSession.default
    watchSession.activate()
    
    guard HKHealthStore.isHealthDataAvailable() else {
      return
    }
    
    let stepsQuantityType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
    let predicate = HKQuery.predicateForSamples(withStart: start, end: end, options: .strictStartDate)
    
    let query = HKSampleQuery(sampleType: stepsQuantityType, predicate: predicate, limit: 0, sortDescriptors: nil) { query, results, error in
      var steps: Double = 0
      if results == nil {return}
      if results!.count > 0
      {
        
        for result in results as! [HKQuantitySample]
        {
          if result.sourceRevision.source.bundleIdentifier == "com.apple.Health" {
            continue;
          }
          if (result.device == nil) {
            continue;
          }
          if WCSession.isSupported() {
            if self.watchSession.isPaired {
              if (result.device?.name?.contains("Watch"))! {
                steps += result.quantity.doubleValue(for: HKUnit.count())
              }
            } else {
              if (result.device?.name?.contains("iPhone"))! {
                steps += result.quantity.doubleValue(for: HKUnit.count())
              } else {
                
              }
            }
          } else {
            if (result.device?.name?.contains("iPhone"))! {
              steps += result.quantity.doubleValue(for: HKUnit.count())
            } else {
              
            }
          }
          
        }
        
        
        completion(steps)
      } else {
        completion(steps)
      }
    }
    
    self.healthStore.execute(query)
  }
}
