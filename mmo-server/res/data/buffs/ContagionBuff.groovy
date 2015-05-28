import shared.board.Board
import shared.board.Buff
import shared.board.DamageType
import shared.board.Unit

def onInit(Buff buff, Board board){
    // Buff has to be added in the spell
}

def onTick(Buff buff, Board board){
    Unit holder = buff.data as Unit;

    def neighbours = board.units.findAll {
        def x = Math.abs(it.position.x - holder.position.x)
        def y = Math.abs(it.position.y - holder.position.y)

        (x <= 1 && y <= 1) && it.state != Unit.STATE_DEAD && !it.hasBuff(buff.buffData.id)
    }

    neighbours.each {
        it.addBuff(board.addBuff(buff.buffData.id, buff.timesToRepeat, buff.period, it))
    }

    holder.doDamage(1, DamageType.POISON)
}

def onEnd(Buff buff, Board board){
    (buff.data as Unit).removeBuff(buff)
}