//
//  Copyright (c) 2018 Loup Inc.
//  Licensed under Apache License v2.0
//

import Foundation
import CoreLocation

struct BeaconRegion: Codable {

    let identifier: String
    let ids: [AnyCodable]

    var proximityUUID: String {
        return ids[0].value as! String
    }

    var major: Int? {
        if ids.count > 1 {
            if let stringValue = ids[1].value as? String {
                if let intValue = Int(stringValue) {
                    return intValue
                }
            }
            return ids[1].value as? Int
        } else {
            return nil
        }
    }

    var minor: Int? {
        if ids.count > 2 {
            if let stringValue = ids[2].value as? String {
                if let intValue = Int(stringValue) {
                    return intValue
                }
            }
            return ids[2].value as? Int
        } else {
            return nil
        }
    }

    init(from region: CLBeaconRegion) {
        self.identifier = region.identifier
        var ids = [
            AnyCodable(region.proximityUUID.uuidString)
        ]

        if let major = region.major?.intValue {
            ids.append(AnyCodable(major))

            if let minor = region.minor?.intValue {
                ids.append(AnyCodable(minor))
            }
        }

        self.ids = ids
    }
}


