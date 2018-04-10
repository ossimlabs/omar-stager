package omar.stager


import com.vividsolutions.jts.geom.Point


class BasicEncyclopedia {

    String activity
    String affiliation
    String be
    String category
    String cc
    String classification
    String codeWord
    String condition
    String dateLastChanged
    String lastUpdated
    Point location
    String msnPrimary
    String name
    String polSubdivision
    String recordStatus
    String releaseMark
    String reviewDate
    String sk
    String suffix
    String symbolId


    // only be, location and suffix are required
    static constraints = {
        activity nullable: true
        affiliation nullable: true
        category nullable: true
        cc nullable: true
        classification nullable: true
        codeWord nullable: true
        condition nullable: true
        dateLastChanged nullable: true
        lastUpdated nullable: true
        msnPrimary nullable: true
        name nullable: true
        polSubdivision nullable: true
        recordStatus nullable: true
        releaseMark nullable: true
        reviewDate nullable: true
        sk nullable: true
        suffix nullable: true
        symbolId nullable: true
    }

    static mapping = {
        be index: "basic_encyclopedia_be_idx"
        location sqlType: "geometry(Point, 4326)"
    }
}
